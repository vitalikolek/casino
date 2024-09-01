package render.casino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CasinoController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
