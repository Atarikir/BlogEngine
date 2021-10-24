package main.controller;

import main.api.response.PostDto;
import main.api.response.PostResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping()
    public ResponseEntity<PostResponse> getAllPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                                    @RequestParam(defaultValue = "10", required = false) int limit,
                                                    @RequestParam(defaultValue = "recent", required = false) String mode) {
        PostResponse postResponse = postService.getPostsForMainPage(offset, limit, mode);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PostResponse> getPostsByQuery(@RequestParam(defaultValue = "0", required = false) int offset,
                                                        @RequestParam(defaultValue = "10", required = false) int limit,
                                                        @RequestParam() String query) {
        PostResponse postResponse = postService.findPostsByQuery(offset, limit, query);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/byDate")
    public ResponseEntity<PostResponse> getPostsByDate(@RequestParam(defaultValue = "0", required = false) int offset,
                                                       @RequestParam(defaultValue = "10", required = false) int limit,
                                                       @RequestParam() String date) {
        PostResponse postResponse = postService.getPostsByDate(offset, limit, date);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/byTag")
    public ResponseEntity<PostResponse> getPostsByTag(@RequestParam(defaultValue = "0", required = false) int offset,
                                                      @RequestParam(defaultValue = "10", required = false) int limit,
                                                      @RequestParam() String tag) {
        PostResponse postResponse = postService.getPostsByTag(offset, limit, tag);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("{ID}")
    public ResponseEntity<PostDto> getPostById(@PathVariable("ID") int id) {
        return postService.getPostById(id);
    }
}
