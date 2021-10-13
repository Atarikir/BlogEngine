package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostDto;
import main.api.response.PostResponse;
import org.springframework.http.ResponseEntity;

public interface PostService {

    PostResponse getPostsForMainPage(int offset, int limit, String mode);

    PostResponse findPostsByQuery(int offset, int limit, String query);

    CalendarResponse getPostsCountByYear(int year);

    PostResponse getPostsByDate(int offset, int limit, String date);

    PostResponse getPostsByTag(int offset, int limit, String tag);

    ResponseEntity<PostDto> getPostById(Integer id);

}
