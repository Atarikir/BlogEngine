package main.controller;

import main.api.request.CreatePostRequest;
import main.api.request.PostModerationRequest;
import main.api.response.PostDto;
import main.api.response.PostResponse;
import main.api.response.ResultErrorResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostResponse> getPostsForModeration(@RequestParam(defaultValue = "0", required = false)
                                                                      int offset,
                                                              @RequestParam(defaultValue = "10", required = false)
                                                                      int limit,
                                                              @RequestParam() String status) {
        PostResponse postResponse = postService.getPostForModeration(offset, limit, status);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostResponse> getMyPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                                   @RequestParam(defaultValue = "10", required = false) int limit,
                                                   @RequestParam() String status) {
        PostResponse postResponse = postService.getMyPost(offset, limit, status);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> addingPost(Principal principal,
                                                          @RequestBody CreatePostRequest createPostRequest) {
        ResultErrorResponse resultErrorResponse = postService.createPost(principal, createPostRequest);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PutMapping("{ID}")
    @PreAuthorize("hasAnyAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> editingPost(Principal principal,
                                                           @RequestBody CreatePostRequest createPostRequest,
                                                           @PathVariable("ID") int id) {
        ResultErrorResponse resultErrorResponse = postService.editPost(principal, createPostRequest, id);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PostMapping("/like")
    @PreAuthorize("hasAnyAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> likePost(@RequestBody PostModerationRequest postModerationRequest,
                                                        Principal principal) {
        ResultErrorResponse resultErrorResponse = postService.likePost(postModerationRequest, principal);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PostMapping("/dislike")
    @PreAuthorize("hasAnyAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> dislikePost(@RequestBody PostModerationRequest postModerationRequest,
                                                           Principal principal) {
        ResultErrorResponse resultErrorResponse = postService.dislikePost(postModerationRequest, principal);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }
}
