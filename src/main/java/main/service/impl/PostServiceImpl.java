package main.service.impl;

import main.api.request.CreatePostRequest;
import main.api.request.PostModerationRequest;
import main.api.response.*;
import main.exceptions.NoFoundException;
import main.model.*;
import main.model.enums.ModerationStatus;
import main.model.enums.SortingMode;
import main.repository.*;
import main.service.AuthCheckService;
import main.service.GeneralService;
import main.service.PostService;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Value("${settings.code.postPremoderation}")
    private String postPremoderation;

    @Value("${settings.value.true}")
    private String settingValueTrue;

    @Value("${settings.value.false}")
    private String settingValueFalse;

    private final Tag2PostRepository tag2PostRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthCheckService authCheckService;
    private final GeneralService generalService;
    private final GlobalSettingRepository globalSettingRepository;
    private final PostVoteRepository postVoteRepository;
    private static final byte IS_ACTIVE = 1;
    private static final int MIN_LENGTH_TITLE = 3;
    private static final int MIN_LENGTH_TEXT = 50;
    private static final String ERROR_TITLE = "Заголовок не установлен";
    private static final String ERROR_TEXT = "Текст публикации слишком короткий";
    private List<String> tags;

    public PostServiceImpl(Tag2PostRepository tag2PostRepository, TagRepository tagRepository,
                           PostRepository postRepository, UserRepository userRepository,
                           AuthCheckService authCheckService, GeneralService generalService,
                           GlobalSettingRepository globalSettingRepository, PostVoteRepository postVoteRepository) {
        this.tag2PostRepository = tag2PostRepository;
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authCheckService = authCheckService;
        this.generalService = generalService;
        this.globalSettingRepository = globalSettingRepository;
        this.postVoteRepository = postVoteRepository;
    }

    @Override
    public PostResponse getPostsForMainPage(int offset, int limit, String mode) {
        Page<Post> posts = getSortedPosts(offset, limit, mode);

        return getPostResponse(posts);
    }

    @Override
    public PostResponse getMyPost(int offset, int limit, String status) {
        Page<Post> posts = getSortedMyPosts(offset, limit, status);

        return getPostResponse(posts);
    }

    @Override
    public PostResponse getPostForModeration(int offset, int limit, String status) {
        Page<Post> posts = getSortedPostsForModeration(offset, limit, status);

        return getPostResponse(posts);
    }

    @Override
    public PostResponse findPostByQuery(int offset, int limit, String query) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts;

        if (!query.isBlank()) {
            posts = postRepository.getPostByQuery(IS_ACTIVE, ModerationStatus.ACCEPTED, generalService.getTimeNow(),
                    query, pageable);
        } else {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    ModerationStatus.ACCEPTED, generalService.getTimeNow(), pageable);
        }

        return getPostResponse(posts);
    }

    @Override
    public CalendarResponse getPostsCountByYear(int year) {
        int currentYear = generalService.getTimeNow().getYear();

        if (year == 0) {
            year = currentYear;
        }

        List<Integer> listYears = postRepository
                .getYears(IS_ACTIVE, ModerationStatus.ACCEPTED, generalService.getTimeNow())
                .stream()
                .sorted()
                .collect(Collectors.toList());

        List<Object[]> postsByYear = postRepository.getPostCountInYearGroupByDate(IS_ACTIVE, ModerationStatus.ACCEPTED,
                generalService.getTimeNow(), year);
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
        Page<Post> posts = postRepository.getPostByDate(IS_ACTIVE, ModerationStatus.ACCEPTED,
                generalService.getTimeNow(), date, pageable);

        return getPostResponse(posts);
    }

    @Override
    public PostResponse getPostByTag(int offset, int limit, String tag) {
        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = postRepository.getPostByTag(IS_ACTIVE, ModerationStatus.ACCEPTED,
                generalService.getTimeNow(), tag, pageable);

        return getPostResponse(posts);
    }

    @Override
    public PostDto getPostById(int id) {
        Post currentPost = postRepository.findById(id);

        if (currentPost == null) {
            throw new NoFoundException();
        }

        List<CommentDto> commentsDtoList = new ArrayList<>();
        List<PostComment> postCommentList = currentPost.getPostComments();
        for (PostComment postComment : postCommentList) {
            User postCommentUser = postComment.getUser();
            commentsDtoList.add(new CommentDto(
                    postComment.getId(),
                    postComment.getTime().toEpochSecond(ZoneOffset.UTC),
                    postComment.getText(),
                    UserDto.builder()
                            .id(postCommentUser.getId())
                            .name(postCommentUser.getName())
                            .photo(postCommentUser.getPhoto())
                            .build()));
        }

        tags = new ArrayList<>();
        List<Tag> tagList = currentPost.getTags();
        tagList.forEach(tag -> tags.add(tag.getName()));

        int viewCount = currentPost.getViewCount();
        int increaseView = 1;
        currentPost.setViewCount(viewCount + increaseView);
        postRepository.save(currentPost);

        PostDto postDto = createPostDto(currentPost);
        postDto.setActive(currentPost.getIsActive() == IS_ACTIVE);
        postDto.setText(currentPost.getText());
        postDto.setAnnounce(null);
        postDto.setCommentCount(null);
        postDto.setComments(commentsDtoList);
        postDto.setTags(tags);

        return postDto;
    }

    @Override
    public ResultErrorResponse createPost(Principal principal, CreatePostRequest createPostRequest) {
        GlobalSetting globalSetting = globalSettingRepository.findByCode(postPremoderation);
        User user = userRepository.findByEmail(principal.getName());
        ModerationStatus moderationStatus = null;

        if (globalSetting.getValue().equals(settingValueTrue) && !user.isModerator()) {
            moderationStatus = ModerationStatus.NEW;
        } else {
            moderationStatus = ModerationStatus.ACCEPTED;
        }

        if (checkTitle(createPostRequest)) {
            return generalService.errorsRequest(
                    ErrorResponse.builder()
                            .title(ERROR_TITLE)
                            .build()
            );
        }

        if (checkText(createPostRequest)) {
            return generalService.errorsRequest(
                    ErrorResponse.builder()
                            .text(ERROR_TEXT)
                            .build()
            );
        }

        Post post = postRepository.save(
                Post.builder()
                        .isActive(createPostRequest.getActive())
                        .moderationStatus(moderationStatus)
                        .user(user)
                        .time(getTimeCreatePost(createPostRequest.getTimestamp()))
                        .title(createPostRequest.getTitle())
                        .text(createPostRequest.getText())
                        .build()
        );

        tags = createPostRequest.getTags();

        saveTagAndPost(tags, post.getId());

        return generalService.getResultTrue();
    }

    @Override
    @Transactional
    public ResultErrorResponse editPost(Principal principal, CreatePostRequest createPostRequest, int id) {
        Post currentPost = postRepository.findById(id);
        ModerationStatus moderationStatus;

        if (currentPost == null) {
            throw new NoFoundException();
        }

        if (checkTitle(createPostRequest)) {
            return generalService.errorsRequest(
                    ErrorResponse.builder()
                            .title(ERROR_TITLE)
                            .build());
        }

        if (checkText(createPostRequest)) {
            return generalService.errorsRequest(
                    ErrorResponse.builder()
                            .text(ERROR_TEXT)
                            .build());
        }

        if (userRepository.findByEmail(principal.getName()).getIsModerator() == 0) {
            moderationStatus = ModerationStatus.NEW;
        } else {
            moderationStatus = currentPost.getModerationStatus();
        }

        currentPost.setIsActive(createPostRequest.getActive());
        currentPost.setModerationStatus(moderationStatus);
        currentPost.setTime(getTimeCreatePost(createPostRequest.getTimestamp()));
        currentPost.setTitle(createPostRequest.getTitle());
        currentPost.setText(createPostRequest.getText());

        postRepository.save(currentPost);
        tag2PostRepository.deleteByPostId(currentPost.getId());

        List<Tag> tagIdList = new ArrayList<>(tagRepository.findAll());

        for (Tag tagId : tagIdList) {
            Integer tag = tag2PostRepository.getTagId(tagId.getId());
            if (tag == null) {
                tagRepository.deleteById(tagId.getId());
            }
        }

        tags = createPostRequest.getTags();

        if (!tags.isEmpty()) {
            saveTagAndPost(tags, currentPost.getId());
        }

        return generalService.getResultTrue();
    }

    @Override
    public ResultErrorResponse likePost(PostModerationRequest postModerationRequest, Principal principal) {

//        if (principal == null) {
//            throw new UnauthorizedException();
//        }

//        if (!authCheckService.isUserAuthorize()) {
//            throw new UnauthorizedException();
//        } else {
//            User currentUser = authCheckService.getAuthorizedUser();
        //}
        String email = principal.getName();
        int id = postModerationRequest.getPostId();
        byte valueVote = 1;
        
        return addValueVote(email, id, valueVote);
    }

    @Override
    public ResultErrorResponse dislikePost(PostModerationRequest postModerationRequest, Principal principal) {
        String email = principal.getName();
        int id = postModerationRequest.getPostId();
        byte valueVote = -1;
        return addValueVote(email, id, valueVote);
    }

    private ResultErrorResponse addValueVote(String email, int id, byte valueVote) {

        User currentUser = userRepository.findByEmail(email);
        Post currentPost = postRepository.findById(id);
        PostVote postVote = postVoteRepository.findByUserAndPost(currentUser, currentPost);
        ResultErrorResponse resultErrorResponse;

        if (postVote == null) {
            save(currentUser, currentPost, valueVote);
            resultErrorResponse = generalService.getResultTrue();
        } else {
            if (postVote.getValue() == valueVote) {
                resultErrorResponse = generalService.getResultFalse();
            } else {
                postVote.setValue(valueVote);
                postVote.setTime(generalService.getTimeNow());
                postVoteRepository.save(postVote);
                resultErrorResponse = generalService.getResultTrue();
            }
        }

        return resultErrorResponse;
    }

    private PostVote save(User user, Post post, byte valueVote) {
        return postVoteRepository.save(
                PostVote.builder()
                        .user(user)
                        .post(post)
                        .time(generalService.getTimeNow())
                        .value(valueVote)
                        .build());
    }

    private void saveTagAndPost(List<String> tags, @NotNull int postId) {
        for (String tagRequest : tags) {
            Tag tag = tagRepository.findByName(tagRequest);

            if (tag == null) {
                tagRepository.save(
                        Tag.builder()
                                .name(tagRequest)
                                .build());
            }

            Tag tag1 = tagRepository.findByName(tagRequest);

            if (tag1 != null) {
                tag2PostRepository.save(
                        Tag2Post.builder()
                                .postId(postId)
                                .tagId(tag1.getId())
                                .build()
                );
            }
        }
    }

    private boolean checkTitle(CreatePostRequest createPostRequest) {
        return createPostRequest.getTitle().length() < MIN_LENGTH_TITLE || createPostRequest.getTitle().isBlank();
    }

    private boolean checkText(CreatePostRequest createPostRequest) {
        return Jsoup.parse(createPostRequest.getText()).text().length() < MIN_LENGTH_TEXT;
    }

    private LocalDateTime getTimeCreatePost(long timestamp) {
        LocalDateTime timeCreatePost;
        LocalDateTime postRequestTime = convertTimestampToLocalDateTime(timestamp);

        if (postRequestTime.isBefore(generalService.getTimeNow())) {
            timeCreatePost = generalService.getTimeNow();
        } else {
            timeCreatePost = postRequestTime;
        }

        return timeCreatePost;
    }

    private Page<Post> getSortedPostsForModeration(int offset, int limit, String status) {
        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = null;
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();

//        @NotNull
//        int userId = userRepository.findByEmail(authCheckService.getLoggedInUser(auth)).getId();
        @NotNull
        int userId = authCheckService.getAuthorizedUser().getId();

        if (status.equals("new")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatus(IS_ACTIVE, ModerationStatus.NEW,
                    pageable);
        } else if (status.equals("declined")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndModeratorId(IS_ACTIVE,
                    ModerationStatus.DECLINED, userId, pageable);
        } else if (status.equals("accepted")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndModeratorId(IS_ACTIVE,
                    ModerationStatus.ACCEPTED, userId, pageable);
        }

        return posts;
    }

    private Page<Post> getSortedMyPosts(int offset, int limit, String status) {
        Pageable pageable = getPageable(offset, limit);
        Page<Post> posts = null;
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = authCheckService.getAuthorizedUser();

        if (status.equals("inactive")) {
            posts = postRepository.findPostsByIsActiveAndUser((byte) 0, user, pageable);
        } else if (status.equals("pending")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, ModerationStatus.NEW,
                    user, pageable);
        } else if (status.equals("declined")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, ModerationStatus.DECLINED,
                    user, pageable);
        } else if (status.equals("published")) {
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE, ModerationStatus.ACCEPTED,
                    user, pageable);
        }

        return posts;
    }

    private Page<Post> getSortedPosts(int offset, int limit, String mode) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> posts = null;

        if (mode.equals(SortingMode.RECENT.toString().toLowerCase())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    ModerationStatus.ACCEPTED, generalService.getTimeNow(), pageable);
        } else if (mode.equals(SortingMode.POPULAR.toString().toLowerCase())) {
            posts = postRepository.getPostsByCommentsCount(IS_ACTIVE, ModerationStatus.ACCEPTED,
                    generalService.getTimeNow(), pageable);
        } else if (mode.equals(SortingMode.BEST.toString().toLowerCase())) {
            posts = postRepository.getPostsByLikesCount(IS_ACTIVE, ModerationStatus.ACCEPTED,
                    generalService.getTimeNow(), pageable);
        } else if (mode.equals(SortingMode.EARLY.toString().toLowerCase())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").ascending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE,
                    ModerationStatus.ACCEPTED, generalService.getTimeNow(), pageable);
        }

        return posts;
    }

    private List<PostDto> getPostsList(Page<Post> posts) {
        List<PostDto> postDtoList = new ArrayList<>();
        for (Post post : posts) {
            postDtoList.add(createPostDto(post));
        }
        return postDtoList;
    }

    private PostDto createPostDto(Post post) {

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

    private String getAnnounce(Post post) {
        String announce = Jsoup.parse(post.getText()).text();
        final int MAX_ANNOUNCE_TEXT_LENGTH = 150;

        if (announce.length() > MAX_ANNOUNCE_TEXT_LENGTH) {
            announce = announce.substring(0, MAX_ANNOUNCE_TEXT_LENGTH).concat("...");
        }

        return announce;
    }

    private PostResponse getPostResponse(Page<Post> posts) {
        List<PostDto> postDtoList = getPostsList(posts);

        PostResponse postResponse = new PostResponse();
        postResponse.setCount((int) posts.getTotalElements());
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    private Pageable getPageable(int offset, int limit) {
        int page = offset / limit;
        return PageRequest.of(page, limit);
    }

    private LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }
}
