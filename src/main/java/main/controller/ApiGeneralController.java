package main.controller;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.response.*;
import main.service.GeneralService;
import main.service.PostService;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final TagService tagService;
    private final SettingsService settingsService;
    private final InitResponse initResponse;
    private final PostService postService;
    private final GeneralService generalService;

    public ApiGeneralController(TagService tagService, SettingsService settingsService,
                                InitResponse initResponse, PostService postService, GeneralService generalService) {
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.initResponse = initResponse;
        this.postService = postService;
        this.generalService = generalService;
    }

    @GetMapping("/init")
    public ResponseEntity<InitResponse> init() {
        return new ResponseEntity<>(initResponse, HttpStatus.OK);
    }

    @GetMapping("/tag")
    public ResponseEntity<TagResponse> getTags(@RequestParam(required = false) String query) {
        TagResponse tagResponse = tagService.getAllTags(query);
        return new ResponseEntity<>(tagResponse, HttpStatus.OK);
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> getSettings() {
        SettingsResponse settingsResponse = settingsService.getGlobalSettings();
        return new ResponseEntity<>(settingsResponse, HttpStatus.OK);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getPostsByYear(int year) {
        CalendarResponse calendarResponse = postService.getPostsCountByYear(year);
        return new ResponseEntity<>(calendarResponse, HttpStatus.OK);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<CommentDto> addComment(@RequestBody CreateCommentRequest createCommentRequest,
                                                 Principal principal) {
        CommentDto commentDto = generalService.createComment(principal, createCommentRequest);
        return new ResponseEntity<>(commentDto, HttpStatus.OK);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultErrorResponse> moderationPost(@RequestBody PostModerationRequest postModerationRequest,
                                                              Principal principal) {
        ResultErrorResponse resultErrorResponse = generalService.postModeration(postModerationRequest, principal);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    //not implemented!!!
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Object> uploadFile(@RequestParam MultipartFile image) throws IOException {
        return new ResponseEntity<>(generalService.uploadImage(image), HttpStatus.OK);
    }

    //not implemented!!!
    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> editingMyProfile() {
        return null;
    }

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticsResponse> myStats(Principal principal) {
        StatisticsResponse statisticsResponse = settingsService.getMyStats(principal);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticsResponse> blogWideStatistics(Principal principal) {
        StatisticsResponse statisticsResponse = settingsService.getStatisticsAllPosts(principal);
        return new ResponseEntity<>(statisticsResponse, HttpStatus.OK);
    }


    /* Проверить как работают все пункты.

        Глобальные настройки блога

        MULTIUSER_MODE — если включен этот режим, в блоге разрешена регистрация новых пользователей.
        Если режим выключен, регистрация пользователей не доступна, на фронте на месте ссылки на страницу
        регистрации появляется текст Регистрация закрыта. При запросе на /api/auth/register необходимо
        возвращать статус 404 (NOT FOUND).

        POST_PREMODERATION - если включен этот режим, то все новые посты пользователей с moderation = false обязательно
        должны попадать на модерацию, у постов при создании должен быть установлен moderation_status = NEW. Eсли
        значения POST_PREMODERATION = false (режим премодерации выключен), то все новые посты должны сразу публиковаться
        (если у них установлен параметр active = 1), у постов при создании должен быть установлен
        moderation_status = ACCEPTED.

        STATISTICS_IS_PUBLIC - если включен этот режим, статистика блога должна быть доступна по запросу
        GET /api/statistics/all для всех групп пользователей. Если режим выключен, по запросу GET/api/statistics/all
        только модераторам отдавать данные статистики. Пользователям и гостям блога необходимо возвращать статус 401.

        Статус коды ответов

        Если пользователь не авторизован при запросе требующего авторизации, возвращайте код 401
        Если при запросе данные (пост, комментарий и прочего) не найдены - 404
        При возврате ответа включающий {result:false} возвращайте код 200

     */
    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public void changeGlobalSettings(@RequestBody SettingsResponse settingsResponse,
                                     Principal principal) {
        settingsService.writeGlobalSettings(settingsResponse, principal);
    }
}
