package main.controller;

import main.api.response.PostResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping()
    private ResponseEntity<PostResponse> posts() {
        PostResponse postResponse = postService.getSortedPosts();
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }
}
