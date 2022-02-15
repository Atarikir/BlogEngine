package main.controller;

import main.api.request.CreateCommentRequest;
import main.api.request.PostModerationRequest;
import main.api.request.ProfileRequest;
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

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Object> uploadFile(@RequestParam MultipartFile image) throws IOException {
        return new ResponseEntity<>(generalService.uploadImage(image), HttpStatus.OK);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> editingMyProfileWithoutPhoto(@RequestBody ProfileRequest profileRequest,
                                                                Principal principal) throws IOException {
        ResultErrorResponse resultErrorResponse = generalService.editMyProfile(profileRequest, principal);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/profile/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultErrorResponse> editingMyProfileWithPhoto(@RequestParam MultipartFile photo,
                                                                @ModelAttribute ProfileRequest profileRequest,
                                                                Principal principal) throws IOException {
        ResultErrorResponse resultErrorResponse = generalService.editMyProfile(photo, profileRequest, principal);
        return new ResponseEntity<>(resultErrorResponse, HttpStatus.OK);
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

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public void changeGlobalSettings(@RequestBody SettingsResponse settingsResponse,
                                     Principal principal) {
        settingsService.writeGlobalSettings(settingsResponse, principal);
    }
}
