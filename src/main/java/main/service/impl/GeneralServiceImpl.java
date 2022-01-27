package main.service.impl;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.response.CommentDto;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.BedRequestException;
import main.exceptions.ImageBadRequestException;
import main.exceptions.TextCommentBadRequestException;
import main.model.Post;
import main.model.PostComment;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.GeneralService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Service
public class GeneralServiceImpl implements GeneralService {

    @Value("${file.uploadDir}")
    private String uploadDir;

    @Value("${file.maxFileSize}")
    private int maxFileSize;

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public GeneralServiceImpl(PostCommentRepository postCommentRepository, PostRepository postRepository,
                              UserRepository userRepository) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public LocalDateTime getTimeNow() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    @Override
    public ResultErrorResponse getResultTrue() {
        return ResultErrorResponse.builder()
                .result(true)
                .build();
    }

    @Override
    public ResultErrorResponse getResultFalse() {
        return ResultErrorResponse.builder()
                .result(false)
                .build();
    }

    @Override
    public ResultErrorResponse errorsRequest(ErrorResponse error) {
        return ResultErrorResponse.builder()
                .result(false)
                .errors(error)
                .build();
    }

    @Override
    public Object uploadImage(MultipartFile image) throws IOException {
        String textErrorExtension = "Файл не формата изображение jpg, png";
        String textErrorSize = "Размер файла превышает допустимый размер";
        String originalFileName = image.getOriginalFilename();
        String fileSuffix = Objects.requireNonNull(originalFileName)
                .substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        if (image.getSize() > maxFileSize) {
            throw new ImageBadRequestException(textErrorSize);
        }

        if (!fileSuffix.equalsIgnoreCase("jpg") && !fileSuffix.equalsIgnoreCase("png")) {
            throw new ImageBadRequestException(textErrorExtension);
        }

        String generatedString = RandomStringUtils.randomAlphabetic(6).toLowerCase();
        String newFileName = RandomStringUtils.randomNumeric(5) + (".") + fileSuffix;

        StringBuilder path = new StringBuilder();
        path
                .append(uploadDir)
                .append(File.separator)
                .append(generatedString, 0, 2)
                .append(File.separator)
                .append(generatedString, 2, 4)
                .append(File.separator)
                .append(generatedString.substring(4))
                .append(File.separator)
                .append(newFileName);

        File destFile = new File(path.toString());

        if (destFile.mkdirs()) {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            ImageIO.write(bufferedImage, fileSuffix, destFile);
        }

        return path.substring(1);
    }

    @Override
    public CommentDto createComment(Principal principal, CreateCommentRequest createCommentRequest) {
        int minLengthComment = 3;
        String textError = "Текст комментария не задан или слишком короткий";
        User user = userRepository.findByEmail(principal.getName());
        Post post = postRepository.findById(createCommentRequest.getPostId());
        String text = createCommentRequest.getText();
        Integer parentId = createCommentRequest.getParentId();

        if (post == null) {
            throw new BedRequestException();
        }

        if (parentId != null) {
            Optional<PostComment> postComment = postCommentRepository.findById(parentId);
            if (postComment.isEmpty()) {
                throw new BedRequestException();
            }
        }

        if (text.length() < minLengthComment || text.isBlank()) {
            throw new TextCommentBadRequestException(textError);
        }

        PostComment postComment = saveComment(createCommentRequest, post, user);

        return CommentDto.builder().id(postComment.getId()).build();
    }

    @Override
    public ResultErrorResponse postModeration(PostModerationRequest postModerationRequest, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        Post currentPost = postRepository.findById(postModerationRequest.getPostId());
        String decision = postModerationRequest.getDecision();

        if (user.isModerator() && currentPost != null && (decision.equals("accept") || decision.equals("decline"))) {
            currentPost.setModerationStatus(getModerationStatus(postModerationRequest));
            currentPost.setModeratorId(user.getId());

            postRepository.save(currentPost);

            return getResultTrue();
        }

        return getResultFalse();
    }

    @Override
    public ResultErrorResponse editMyProfile(MultipartFile photo, String name, String email, String password,
                                             int removePhoto) {
        userRepository.save(
                User.builder()
                        .photo()
                        .name()
                        .email()
                        .password()
                        .build());

        return getResultTrue();
    }

    private ModerationStatus getModerationStatus(PostModerationRequest postModerationRequest) {
        ModerationStatus moderationStatus = null;

        if (postModerationRequest.getDecision().equals("accept")) {
            moderationStatus = ModerationStatus.ACCEPTED;
        }

        if (postModerationRequest.getDecision().equals("decline")) {
            moderationStatus = ModerationStatus.DECLINED;
        }

        return moderationStatus;
    }

    private PostComment saveComment(CreateCommentRequest createCommentRequest, Post post, User user) {
        return postCommentRepository.save(
                PostComment.builder()
                        .parentId(createCommentRequest.getParentId())
                        .post(post)
                        .user(user)
                        .time(getTimeNow())
                        .text(createCommentRequest.getText())
                        .build()
        );
    }
}
