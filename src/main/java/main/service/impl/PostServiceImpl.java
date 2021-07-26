package main.service.impl;

import main.api.response.PostDto;
import main.api.response.PostResponse;
import main.repository.PostRepository;
import main.service.PostService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostResponse getSortedPosts() {
        PostResponse postResponse = new PostResponse();
        List<PostDto> postDtoList = new ArrayList<>();
        postResponse.setCount(0);
        postResponse.setPosts(postDtoList);
        return postResponse;
    }
}
