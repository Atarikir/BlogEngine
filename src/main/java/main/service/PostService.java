package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostDto;
import main.api.response.PostResponse;

public interface PostService {

    PostResponse getPostsForMainPage(int offset, int limit, String mode);

    PostResponse findPostByQuery(int offset, int limit, String query);

    CalendarResponse getPostsCountByYear(int year);

    PostResponse getPostByDate(int offset, int limit, String date);

    PostResponse getPostByTag(int offset, int limit, String tag);

    PostDto getPostById(Integer id);

    PostResponse getMyPost(int offset, int limit, String status);
}
