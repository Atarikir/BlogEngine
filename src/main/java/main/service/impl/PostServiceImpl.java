package main.service.impl;

import main.api.response.*;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.model.enums.SortingMode;
import main.repository.PostRepository;
import main.service.PostService;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final byte isActive = 1;
    private final ModerationStatus moderationStatus = ModerationStatus.ACCEPTED;
    private final LocalDateTime time = LocalDateTime.now();

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
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
    public PostResponse findPostsByQuery(int offset, int limit, String query) {

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts;

        if (!query.isBlank()) {
            posts = postRepository.getPostByQuery(isActive, moderationStatus, time, query, pageable);
        } else {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus, time,
                    pageable);
        }

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    public Page<Post> getSortedPosts(int offset, int limit, String mode) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts = null;

        if (mode.equals(SortingMode.recent.toString())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus, time,
                    pageable);
        } else if (mode.equals(SortingMode.popular.toString())) {
            posts = postRepository.getPostsByCommentsCount(isActive, moderationStatus, time, pageable);
        } else if (mode.equals(SortingMode.best.toString())) {
            posts = postRepository.getPostsByLikesCount(isActive, moderationStatus, time, pageable);
        } else if (mode.equals(SortingMode.early.toString())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").ascending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus, time,
                    pageable);
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

        final int CURRENT_YEAR = LocalDateTime.now().getYear();

        if (year == 0) {
            year = CURRENT_YEAR;
        }

        List<Integer> listYears = postRepository.getYears(isActive, moderationStatus, time)
                .stream().sorted().collect(Collectors.toList());

        List<Object[]> postsByYear = postRepository.getPostCountInYearGroupByDate(isActive, moderationStatus, time, year);
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
    public PostResponse getPostsByDate(int offset, int limit, String date) {

        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = postRepository.getPostByDate(isActive, moderationStatus, time, date, pageable);

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    @Override
    public PostResponse getPostsByTag(int offset, int limit, String tag) {

        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = postRepository.getPostByTag(isActive, moderationStatus, time, tag, pageable);

        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    @Override
    public ResponseEntity<PostDto> getPostById(Integer id) {

        Post currentPost = postRepository.findPostByIsActiveAndModerationStatusAndTimeBeforeAndId(isActive,
                moderationStatus, time, id);

        if (currentPost == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        currentPost.setViewCount(viewCount + 1);
        postRepository.save(currentPost);

        PostDto postDto = createPostDto(currentPost);
        postDto.setActive(currentPost.getIsActive() == 1);
        postDto.setText(currentPost.getText());
        postDto.setAnnounce(null);
        postDto.setCommentCount(null);
        postDto.setComments(commentsDtoList);
        postDto.setTags(tags);

        return ResponseEntity.ok(postDto);
    }

    public Pageable getPageable(int offset, int limit) {
        int page = offset / limit;
        return PageRequest.of(page, limit);
    }
}
