package main.service.impl;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.response.CommentDto;
import main.api.response.ErrorResponse;
import main.api.response.ResultErrorResponse;
import main.exceptions.BedRequestException;
import main.exceptions.TextCommentNotFoundException;
import main.model.Post;
import main.model.PostComment;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostCommentRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.GeneralService;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class GeneralServiceImpl implements GeneralService {

    @Value("${upload.dir}")
    private String uploadDir;

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
    public ResultErrorResponse errorsRequest(ErrorResponse errors) {
        return ResultErrorResponse.builder()
                .result(false)
                .errors(errors)
                .build();
    }

    @Override
    public Object uploadImage(MultipartFile image) {

//        try {
//            if (image != null) {
////            File uploadImage = new File(uploadDir);
////
////            if (!uploadImage.exists()) {
////                uploadImage.mkdir();
////            }
//
//                String uuidFile = UUID.randomUUID().toString();
//                String fileName = uuidFile.substring(24) + "." + image.getOriginalFilename();
//                String resultFileName = uploadDir + "/" + uuidFile.substring(9, 13) + "/" + uuidFile.substring(14, 18) + "/" +
//                        uuidFile.substring(19, 23) + fileName;
//                String filePath = "/home/pandora/Pictures/";
//
//                File path = new File(filePath);
//
//                if (!path.exists()) {
//                    path.mkdir();
//                }
//
//                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
//                        new File(filePath + fileName)));
//
//                out.write(image.getBytes());
//                out.flush();
//                out.close();
//
//                return resultFileName;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;

//                @PostMapping("/upload")
//        public ResponseVO<String> upload(@RequestParam("image") MultipartFile image) {
//            ResponseVO<String> responseVO = new ResponseVO<>();
//            try {
//                if (image.isEmpty()) {
//                    responseVO.setCode(1);
//                    responseVO.setMessage(«Файл пуст»);
//                } else {
//                    String dotExtendName = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));// Получить расширение
//                    String fileName = UUID.randomUUID().toString().replace("-", "") + dotExtendName;// Имя UUID + расширение.
//                    String filePath = "D:/image/";
//                    // Создаем каталог, чтобы не найти путь
//                    File path = new File(filePath);
//                    if (!path.exists()) {
//                        path.mkdirs();
//                    }
//                    // Загрузить
//                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filePath + fileName)));
//                    out.write(image.getBytes());
//                    out.flush();
//                    out.close();
//                    responseVO.setCode(0);
//                    responseVO.setMessage(«Файл загружен успешно»);
//                    responseVO.setData(fileName);// Имя файла echo
//                }
//            } catch (Exception ex) {
//                responseVO.setCode(2);
//                responseVO.setMessage(«Ошибка загрузки файла» + ex.getMessage());
//                // Запись журнала
//            }
//            return responseVO;
//        }

//        @Service
//        public class FileService {
//
//            @Value("${app.upload.dir:${user.home}}")
//            public String uploadDir;
//
//            public void uploadFile(MultipartFile image) {
//
//                try {
//                    Path copyLocation = Paths
//                            .get(uploadDir + File.separator + StringUtils.cleanPath(image.getOriginalFilename()));
//                    Files.copy(image.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new FileStorageException("Could not store image " + image.getOriginalFilename()
//                            + ". Please try again!");
//                }
//            }
//        }
    }

    @Override
    public CommentDto createComment(Principal principal, CreateCommentRequest createCommentRequest) {
        int minLengthComment = 3;
        String textError = "Текст комментария не задан или слишком короткий";
        User user = userRepository.findByEmail(principal.getName());
        Post post = postRepository.findById(createCommentRequest.getPostId());
        String text = Jsoup.parse(createCommentRequest.getText()).text();
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
            throw new TextCommentNotFoundException(textError);
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
