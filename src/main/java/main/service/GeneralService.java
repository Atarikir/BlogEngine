package main.service;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.response.CommentDto;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

public interface GeneralService {

    LocalDateTime getTimeNow();

    ResultErrorResponse getResultTrue();

    ResultErrorResponse getResultFalse();

    ResultErrorResponse errorsRequest(ErrorResponse errors);

    Object uploadImage(MultipartFile image) throws IOException;

    CommentDto createComment(Principal principal, CreateCommentRequest createCommentRequest);

    ResultErrorResponse postModeration(PostModerationRequest postModerationRequest, Principal principal);

    ResultErrorResponse editMyProfile(MultipartFile photo, String name, String email, String password, int removePhoto);
}
