package main.service;

import main.api.response.TagResponse;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    public TagResponse getTags() {
        TagResponse tagResponse = new TagResponse();
        //tagResponse.getTags();
        return tagResponse;
    }
}
