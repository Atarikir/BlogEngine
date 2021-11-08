package main.service.impl;

import main.api.response.*;
import main.exceptions.NoFoundException;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.model.enums.SortingMode;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthCheckService;
import main.service.PostService;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthCheckService authCheckService;
    private static final byte IS_ACTIVE = 1;
    private static final ModerationStatus MODERATION_STATUS_ACCEPTED = ModerationStatus.ACCEPTED;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository,
                           AuthCheckService authCheckService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authCheckService = authCheckService;
    }

    @Override
    public PostResponse getMyPost(int offset, int limit, String status) {
        Page<Post> posts = getSortedMyPosts(offset, limit, status);
        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);
        return postResponse;
    }

    @Override
    public PostResponse getPostsForMainPage(int offset, int limit, String mode) {
        Page<Post> posts = getSortedPosts(offset, limit, mode);
        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    @Override
    public PostResponse findPostByQuery(int offset, int limit, String query) {

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts;

        if (!query.isBlank()) {
            posts = postRepository.getPostByQuery(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now(), query,
                    pageable);
        } else {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    MODERATION_STATUS_ACCEPTED, LocalDateTime.now(), pageable);
        }

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    public Page<Post> getSortedMyPosts(int offset, int limit, String status) {
        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authCheckService.getLoggedInUser(auth));

        if (status.equals("inactive")) {
            posts = postRepository.findPostsByIsActiveAndUser((byte) 0, user, pageable);
        } else if (status.equals("pending")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, ModerationStatus.NEW,
                    user, pageable);
        } else if (status.equals("declined")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, ModerationStatus.DECLINED,
                    user, pageable);
        } else if (status.equals("published")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, MODERATION_STATUS_ACCEPTED,
                    user, pageable);
        }

        return posts;
    }

    public Page<Post> getSortedPosts(int offset, int limit, String mode) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts = null;

        if (mode.equals(SortingMode.RECENT.toString().toLowerCase())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    MODERATION_STATUS_ACCEPTED, LocalDateTime.now(), pageable);
        } else if (mode.equals(SortingMode.POPULAR.toString().toLowerCase())) {
            posts = postRepository.getPostsByCommentsCount(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now(),
                    pageable);
        } else if (mode.equals(SortingMode.BEST.toString().toLowerCase())) {
            posts = postRepository.getPostsByLikesCount(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now(),
                    pageable);
        } else if (mode.equals(SortingMode.EARLY.toString().toLowerCase())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").ascending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    MODERATION_STATUS_ACCEPTED, LocalDateTime.now(), pageable);
        }

        return posts;
    }

    public List<PostDto> getPostsList(Page<Post> posts) {
        List<PostDto> postDtoList = new ArrayList<>();
        for (Post post : posts) {
            postDtoList.add(createPostDto(post));
        }
        return postDtoList;
    }

    public PostDto createPostDto(Post post) {

        return PostDto.builder()
                .id(post.getId())
                .timestamp(post.getTime().toEpochSecond(ZoneOffset.UTC))
                .user(UserDto.builder()
                        .id(post.getUser().getId())
                        .name(post.getUser().getName())
                        .build())
                .title(post.getTitle())
                .announce(getAnnounce(post))
                .likeCount((int) post.getPostVotes().stream().filter(postVote -> postVote.getValue() == 1).count())
                .dislikeCount((int) post.getPostVotes().stream().filter(postVote -> postVote.getValue() == -1).count())
                .commentCount(post.getPostComments().size())
                .viewCount(post.getViewCount())
                .build();
    }

    public String getAnnounce(Post post) {
        String announce = Jsoup.parse(post.getText()).text();

        final int MAX_ANNOUNCE_TEXT_LENGTH = 150;
        if (announce.length() > MAX_ANNOUNCE_TEXT_LENGTH) {
            announce = announce.substring(0, MAX_ANNOUNCE_TEXT_LENGTH).concat("...");
        }
        return announce;
    }

    @Override
    public CalendarResponse getPostsCountByYear(int year) {

        int currentYear = LocalDateTime.now().getYear();

        if (year == 0) {
            year = currentYear;
        }

        List<Integer> listYears = postRepository.getYears(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now())
                .stream().sorted().collect(Collectors.toList());

        List<Object[]> postsByYear = postRepository.getPostCountInYearGroupByDate(IS_ACTIVE, MODERATION_STATUS_ACCEPTED,
                LocalDateTime.now(), year);
        Map<String, Long> mapPosts = new TreeMap<>();

        for (Object[] post : postsByYear) {
            String date = post[0].toString();
            long count = (long) post[1];
            mapPosts.put(date, count);
        }

        CalendarResponse calendarResponse = new CalendarResponse();
        calendarResponse.setYears(listYears);
        calendarResponse.setPosts(mapPosts);

        return calendarResponse;
    }

    @Override
    public PostResponse getPostByDate(int offset, int limit, String date) {

        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = postRepository.getPostByDate(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now(),
                date, pageable);

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    @Override
    public PostResponse getPostByTag(int offset, int limit, String tag) {

        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = postRepository.getPostByTag(IS_ACTIVE, MODERATION_STATUS_ACCEPTED, LocalDateTime.now(),
                tag, pageable);

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    @Override
    public PostDto getPostById(Integer id) {

        Post currentPost = postRepository.findPostByIsActiveAndModerationStatusAndTimeBeforeAndId(IS_ACTIVE,
                MODERATION_STATUS_ACCEPTED, LocalDateTime.now(), id);

        if (currentPost == null) {
            throw new NoFoundException();
        }

        List<CommentsDto> commentsDtoList = new ArrayList<>();
        List<PostComment> postCommentList = currentPost.getPostComments();
        for (PostComment postComment : postCommentList) {
            User postCommentUser = postComment.getUser();
            commentsDtoList.add(new CommentsDto(
                    postComment.getId(),
                    postComment.getTime().toEpochSecond(ZoneOffset.UTC),
                    postComment.getText(),
                    UserDto.builder()
                            .id(postCommentUser.getId())
                            .name(postCommentUser.getName())
                            .photo(postCommentUser.getPhoto())
                            .build()));
        }

        List<String> tags = new ArrayList<>();
        List<Tag> tagList = currentPost.getTags();
        tagList.forEach(tag -> tags.add(tag.getName()));

        int viewCount = currentPost.getViewCount();
        int increaseView = 1;
        currentPost.setViewCount(viewCount + increaseView);
        postRepository.save(currentPost);

        PostDto postDto = createPostDto(currentPost);
        postDto.setActive(currentPost.getIsActive() == 1);
        postDto.setText(currentPost.getText());
        postDto.setAnnounce(null);
        postDto.setCommentCount(null);
        postDto.setComments(commentsDtoList);
        postDto.setTags(tags);

        return postDto;
    }

    public Pageable getPageable(int offset, int limit) {
        int page = offset / limit;
        return PageRequest.of(page, limit);
    }
}
