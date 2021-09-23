package main.service.impl;

import main.api.response.PostDto;
import main.api.response.PostResponse;
import main.api.response.UserDto;
import main.model.Post;
import main.model.enums.ModerationStatus;
import main.model.enums.SortingMode;
import main.repository.PostRepository;
import main.service.PostService;
import org.jsoup.Jsoup;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostResponse getPostsForMainPage(int offset, int limit, String mode) {
        PostResponse postResponse = new PostResponse();
        List<Post> posts = getSortedPosts(offset, limit, mode);
        List<PostDto> postDtoList = getPostList(posts);

        int count = postRepository.countByIsActiveAndModerationStatusAndTimeBefore((byte) 1, ModerationStatus.ACCEPTED,
                LocalDateTime.now());
        postResponse.setCount(count);
        postResponse.setPosts(postDtoList);

        return postResponse;
    }

    public List<Post> getSortedPosts(int offset, int limit, String mode) {
        int page = offset / limit;
        List<Post> posts = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, limit);

        if (mode.equals(SortingMode.recent.toString())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore((byte) 1,
                    ModerationStatus.ACCEPTED, LocalDateTime.now(), pageable);
        } else if (mode.equals(SortingMode.popular.toString())) {
            posts = postRepository.getPostsByCommentsCount((byte) 1,
                    ModerationStatus.ACCEPTED, LocalDateTime.now(), pageable);
        } else if (mode.equals(SortingMode.best.toString())) {
            posts = postRepository.getPostsByLikesCount((byte) 1,
                    ModerationStatus.ACCEPTED, LocalDateTime.now(), pageable);
        } else if (mode.equals(SortingMode.early.toString())) {
            pageable = PageRequest.of(page, limit, Sort.by("time").ascending());
            posts = postRepository.findPostsByIsActiveAndModerationStatusAndTimeBefore((byte) 1,
                    ModerationStatus.ACCEPTED, LocalDateTime.now(), pageable);
        }

        return posts;
    }

    public List<PostDto> getPostList(List<Post> posts) {
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
                .userDto(UserDto.builder()
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

        int MAX_ANNOUNCE_TEXT_LENGTH = 150;
        if (announce.length() > MAX_ANNOUNCE_TEXT_LENGTH) {
            announce = announce.substring(0, MAX_ANNOUNCE_TEXT_LENGTH).concat("...");
        }
        return announce;
    }
}
