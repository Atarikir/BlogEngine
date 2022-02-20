package main.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.request.ProfileRequest;
import main.api.response.CommentDto;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.*;
import main.model.Post;
import main.model.PostComment;
import main.model.User;
import main.model.enums.Message;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.GeneralService;
import main.service.UtilityService;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class GeneralServiceImpl implements GeneralService {

    @Value("${file.maxFileSize}")
    private int maxFileSize;

    @Value("${user.namePattern}")
    private String namePattern;

    @Value("${user.minLengthPassword}")
    private int minLengthPassword;

    @Value("${file.width}")
    private int photoWidth;

    @Value("${file.height}")
    private int photoHeight;

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UtilityService utilityService;
    private final Cloudinary cloudinary;

    public GeneralServiceImpl(PostCommentRepository postCommentRepository, PostRepository postRepository,
                              UserRepository userRepository, UtilityService utilityService, Cloudinary cloudinary) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.utilityService = utilityService;
        this.cloudinary = cloudinary;
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
            if (user.getPhoto() != null) {
                String publicIdCloudImage = user.getPhoto().substring(60, 80);
                log.info(publicIdCloudImage);
                cloudinary.uploader().destroy(publicIdCloudImage, ObjectUtils.emptyMap());
                user.setPhoto(null);
            } else {
                throw new NoFoundException();
            }
        }

        if (name == null || name.isEmpty() || !name.matches(namePattern)) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .name(Message.ERROR_NAME.getText())
                            .build()
            );
        } else {
            user.setName(name);
        }

        if (email == null) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .email("Поле не заполнено. Введите ваш email")
                            .build()
            );
        }

        if (!email.equals(user.getEmail()) && userRepository.findByEmail(email) != null) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .email(Message.ERROR_EMAIL.getText())
                            .build()
            );
        } else if (email.equals(user.getEmail())) {
            user.setEmail(email);
        } else {
            user.setEmail(email);
            SecurityContextHolder.clearContext();
        }

        if (password == null) {
            user.setPassword(user.getPassword());
        } else if (password.length() < minLengthPassword) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .password(Message.ERROR_PASSWORD.getText())
                            .build()
            );
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
                            .photo(Message.ERROR_EXTENSION.getText())
                            .build()
            );
        }

        if (photo.getSize() > maxFileSize) {
            return utilityService.errorsRequest(
                    ErrorResponse.builder()
                            .photo(Message.ERROR_PHOTO.getText())
                            .build()
            );
        } else {
            user.setPhoto(uploadAvatar(photo));
        }

        return editMyProfile(profileRequest, principal);
    }

    @Override
    public Object uploadImage(MultipartFile image) {
        String textErrorSize = "Размер файла превышает допустимый размер";
        String fileSuffix = getFileSuffix(image);

        if (image.getSize() > maxFileSize) {
            throw new ImageBadRequestException(textErrorSize);
        }

        if (!fileSuffix.equalsIgnoreCase("jpg") && !fileSuffix.equalsIgnoreCase("png")) {
            throw new ImageBadRequestException(Message.ERROR_EXTENSION.getText());
        }

        try {
            File uploadedFile = convertMultiPartToFile(image);
            return getUploadResult(uploadedFile).get("url").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadAvatar(MultipartFile photo) {

        try {
            File uploadedFile = resizeMultiPartToFile(photo);
            return getUploadResult(uploadedFile).get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map getUploadResult(File uploadedFile) throws IOException {

        Map uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.emptyMap());

        if (uploadedFile.delete()) {
            log.info("File successfully deleted");
        } else
            log.info("File doesn't exist");
        return uploadResult;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String fileSuffix = getFileSuffix(file);
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        ImageIO.write(bufferedImage, fileSuffix, convFile);
        return convFile;
    }

    private File resizeMultiPartToFile(MultipartFile file) throws IOException {
        String fileSuffix = getFileSuffix(file);
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        BufferedImage resizeAvatar = resizePhoto(file);
        ImageIO.write(resizeAvatar, fileSuffix, convFile);
        return convFile;
    }

    private String getFileSuffix(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        return Objects.requireNonNull(originalFileName)
                .substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private BufferedImage resizePhoto(MultipartFile photo) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(photo.getInputStream());

        return Scalr.resize(bufferedImage,
                Scalr.Method.ULTRA_QUALITY,
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
