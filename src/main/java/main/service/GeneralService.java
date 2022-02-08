package main.service;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.request.ProfileRequest;
import main.api.response.CommentDto;
import main.api.response.ResultErrorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

public interface GeneralService {
    Object uploadImage(MultipartFile image) throws IOException;

    CommentDto createComment(Principal principal, CreateCommentRequest createCommentRequest);

    ResultErrorResponse postModeration(PostModerationRequest postModerationRequest, Principal principal);

    ResultErrorResponse editMyProfile(ProfileRequest profileRequest, Principal principal) throws IOException;

    ResultErrorResponse editMyProfile(MultipartFile photo, ProfileRequest profileRequest, Principal principal) throws IOException;
}
