package main.service.impl;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.request.ProfileRequest;
import main.api.response.CommentDto;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.BedRequestException;
import main.exceptions.ImageBadRequestException;
import main.exceptions.TextCommentBadRequestException;
import main.exceptions.UnauthorizedException;
import main.model.Post;
import main.model.PostComment;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.GeneralService;
import main.service.UtilityService;
import org.apache.commons.lang3.RandomStringUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

@Service
public class GeneralServiceImpl implements GeneralService {

    @Value("${file.uploadDirImage}")
    private String uploadDirImage;

    @Value("${file.uploadDirAvatar}")
    private String uploadDirAvatar;

    @Value("${file.maxFileSize}")
    private int maxFileSize;

    @Value("${user.namePattern}")
    private String namePattern;

    @Value("${user.minLengthPassword}")
    private int minLengthPassword;

    @Value("${user.errorEmail}")
    private String errorEmail;

    @Value("${user.errorName}")
    private String errorName;

    @Value("${user.errorPassword}")
    private String errorPassword;

    @Value("${user.errorPhoto}")
    private String errorPhoto;

    @Value("${user.errorExtension}")
    private String errorExtension;

    @Value("${file.width}")
    private int photoWidth;

    @Value("${file.height}")
    private int photoHeight;

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UtilityService utilityService;

    public GeneralServiceImpl(PostCommentRepository postCommentRepository, PostRepository postRepository,
                              UserRepository userRepository, UtilityService utilityService) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.utilityService = utilityService;
    }

    @Override
    public Object uploadImage(MultipartFile image) throws IOException {
        String textErrorSize = "Размер файла превышает допустимый размер";
        String fileSuffix = getFileSuffix(image);

        if (image.getSize() > maxFileSize) {
            throw new ImageBadRequestException(textErrorSize);
        }

        if (!fileSuffix.equalsIgnoreCase("jpg") && !fileSuffix.equalsIgnoreCase("png")) {
            throw new ImageBadRequestException(errorExtension);
        }

        String path = createPath(fileSuffix);
        File destFile = new File(path);

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
        if (principal == null)
            throw new UnauthorizedException();

        User user = userRepository.findByEmail(principal.getName());
        Post currentPost = postRepository.findById(postModerationRequest.getPostId());
        String decision = postModerationRequest.getDecision();

        if (user.isModerator() && currentPost != null && (decision.equals("accept") || decision.equals("decline"))) {
            currentPost.setModerationStatus(getModerationStatus(postModerationRequest));
            currentPost.setModeratorId(user.getId());

            postRepository.save(currentPost);

            return utilityService.getResultTrue();
        }

        return utilityService.getResultFalse();
    }

    @Override
    public ResultErrorResponse editMyProfile(ProfileRequest profileRequest, Principal principal) throws IOException {
        User user = userRepository.findByEmail(principal.getName());
        String name = profileRequest.getName();
        String email = profileRequest.getEmail();
        String password = profileRequest.getPassword();

        if (profileRequest.getRemovePhoto() == 1) {
            Files.delete(Path.of("." + user.getPhoto()));
            user.setPhoto("");
        }

        if (name == null || name.isEmpty() || !name.matches(namePattern)) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .name(errorName)
                            .build()
            );
        } else {
            user.setName(name);
        }

        if (!email.equals(user.getEmail()) && userRepository.findByEmail(email) != null) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .email(errorEmail)
                            .build());
        } else {
            user.setEmail(email);
        }

        if (password == null) {
            user.setPassword(user.getPassword());
        } else if (password.length() < minLengthPassword) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .password(errorPassword)
                            .build());
        } else {
            user.setPassword(utilityService.encodeBCrypt(password));
        }

        userRepository.save(user);

        return utilityService.getResultTrue();
    }

    @Override
    public ResultErrorResponse editMyProfile(MultipartFile photo, ProfileRequest profileRequest, Principal principal)
            throws IOException {
        User user = userRepository.findByEmail(principal.getName());
        String fileSuffix = getFileSuffix(photo);

        if (!fileSuffix.equalsIgnoreCase("jpg") && !fileSuffix.equalsIgnoreCase("png")) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .photo(errorExtension)
                            .build()
            );
        }

        if (photo.getSize() > maxFileSize) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .photo(errorPhoto)
                            .build()
            );
        } else {
            user.setPhoto(uploadAvatar(photo));
        }

        return editMyProfile(profileRequest, principal);
    }

    private String uploadAvatar(MultipartFile photo) throws IOException {
        String fileSuffix = getFileSuffix(photo);
        String path = createPath(fileSuffix);
        File destFile = new File(path);

        if (destFile.mkdirs()) {
            BufferedImage bufferedImage = ImageIO.read(photo.getInputStream());
            BufferedImage resizeAvatar = resizePhoto(bufferedImage);
            ImageIO.write(resizeAvatar, fileSuffix, destFile);
        }

        return path.substring(1);
    }

    private String getFileSuffix(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        return Objects.requireNonNull(originalFileName)
                .substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String createPath(String fileSuffix) {
        String generatedString = RandomStringUtils.randomAlphabetic(6).toLowerCase();
        String newFileName = RandomStringUtils.randomNumeric(5) + (".") + fileSuffix;
        return uploadDirAvatar +
                File.separator +
                generatedString.substring(0, 2) +
                File.separator +
                generatedString.substring(2, 4) +
                File.separator +
                generatedString.substring(4) +
                File.separator +
                newFileName;
    }

    private BufferedImage resizePhoto(BufferedImage photo) {

        return Scalr.resize(photo,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_EXACT,
                photoWidth,
                photoHeight,
                Scalr.OP_ANTIALIAS);
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
                        .time(utilityService.getTimeNow())
                        .text(createCommentRequest.getText())
                        .build()
        );
    }
}
