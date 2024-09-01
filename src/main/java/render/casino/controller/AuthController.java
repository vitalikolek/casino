package render.casino.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import render.casino.repository.UserRepository;
import render.casino.security.service.UserDetailsImpl;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "signup")
    public String signupController(Authentication authentication, Model model,
                                   @RequestParam(value = "ref", required = false) String ref, @RequestParam(value = "promo", required = false) String promo, @RequestParam(value = "error", required = false) String error) {
        if (isAuthorized(authentication)) {
            return "redirect:/";
        }

        model.addAttribute("ref", ref);

        model.addAttribute("promo", promo);

        model.addAttribute("error", error);

        return "signup";
    }

    @GetMapping(value = "signin")
    public String signinController(Authentication authentication, Model model, @RequestParam(value = "error", required = false) String error) {
        if (isAuthorized(authentication)) {
            return "redirect:/";
        }

        model.addAttribute("error", error);

        return "signin";
    }

    private boolean isAuthorized(Authentication authentication) {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userRepository.existsById(userDetails.getId());
        }
        return false;
    }
}
