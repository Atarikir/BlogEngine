package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DefaultController {

    @GetMapping(value = "/**/{path:[^\\.]*}")
    public String redirectToIndex(@PathVariable String path) {
        return "forward:/";
    }
}
