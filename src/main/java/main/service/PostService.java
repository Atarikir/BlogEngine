package main.service;

import main.api.response.PostResponse;

public interface PostService {

    PostResponse getPostsForMainPage(int offset, int limit, String mode);
}
