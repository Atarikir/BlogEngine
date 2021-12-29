package main.service;

import main.api.request.CreatePostRequest;
import main.api.response.*;

import java.security.Principal;

public interface PostService {

    PostResponse getPostsForMainPage(int offset, int limit, String mode);

    PostResponse findPostByQuery(int offset, int limit, String query);

    CalendarResponse getPostsCountByYear(int year);

    PostResponse getPostByDate(int offset, int limit, String date);

    PostResponse getPostByTag(int offset, int limit, String tag);

    PostDto getPostById(int id);

    PostResponse getMyPost(int offset, int limit, String status);

    ResultErrorResponse createPost(Principal principal, CreatePostRequest createPostRequest);

    PostResponse getPostForModeration(int offset, int limit, String status);

    ResultErrorResponse editPost(Principal principal, CreatePostRequest createPostRequest, int id);
}
