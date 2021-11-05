package main.controller;

import main.api.response.PostDto;
import main.api.response.PostResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        PostResponse postResponse = postService.findPostByQuery(offset, limit, query);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/byDate")
    public ResponseEntity<PostResponse> getPostsByDate(@RequestParam(defaultValue = "0", required = false) int offset,
                                                       @RequestParam(defaultValue = "10", required = false) int limit,
                                                       @RequestParam() String date) {
        PostResponse postResponse = postService.getPostByDate(offset, limit, date);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/byTag")
    public ResponseEntity<PostResponse> getPostsByTag(@RequestParam(defaultValue = "0", required = false) int offset,
                                                      @RequestParam(defaultValue = "10", required = false) int limit,
                                                      @RequestParam() String tag) {
        PostResponse postResponse = postService.getPostByTag(offset, limit, tag);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("{ID}")
    public ResponseEntity<PostDto> getPostById(@PathVariable("ID") int id) {
        PostDto postDto = postService.getPostById(id);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostResponse> getMyPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                                   @RequestParam(defaultValue = "10", required = false) int limit,
                                                   @RequestParam() String status) {
        PostResponse postResponse = postService.getMyPost(offset, limit, status);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }
}
