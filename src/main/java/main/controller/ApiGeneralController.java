package main.controller;

import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final TagService tagService;
    private final SettingsService settingsService;
    private final InitResponse initResponse;

    public ApiGeneralController(TagService tagService, SettingsService settingsService,
                                InitResponse initResponse) {
        this.tagService = tagService;
        this.settingsService = settingsService;
        this.initResponse = initResponse;
    }


    @GetMapping("/tag")
    private ResponseEntity<TagResponse> tags() {
        return new ResponseEntity<>(tagService.getTags(), HttpStatus.OK);
    }

    @GetMapping("/settings")
    private ResponseEntity<SettingsResponse> settings() {
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/init")
    private InitResponse init() {
        //System.out.println(initResponse.getTitle());
        return initResponse;
    }
}
