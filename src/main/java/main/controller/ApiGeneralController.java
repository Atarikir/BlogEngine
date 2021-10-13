package main.controller;

import main.api.response.CalendarResponse;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.PostService;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final TagService tagService;
    private final SettingsService settingsService;
    private final InitResponse initResponse;
    private final PostService postService;

    public ApiGeneralController(TagService tagService, SettingsService settingsService,
                                InitResponse initResponse, PostService postService) {
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.initResponse = initResponse;
        this.postService = postService;
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

    @GetMapping("/init")
    public ResponseEntity<InitResponse> init() {
        return new ResponseEntity<>(initResponse, HttpStatus.OK);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getPostsByYear(int year) {
        CalendarResponse calendarResponse = postService.getPostsCountByYear(year);
        return new ResponseEntity<>(calendarResponse, HttpStatus.OK);
    }
}
