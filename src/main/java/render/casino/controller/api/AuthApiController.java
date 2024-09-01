package render.casino.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import render.casino.model.User;
import render.casino.payload.request.LoginRequest;
import render.casino.payload.request.RegisterRequest;
import render.casino.payload.response.UserInfoResponse;
import render.casino.repository.UserRepository;
import render.casino.security.jwt.JwtUtils;
import render.casino.security.service.UserDetailsImpl;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail().toLowerCase())
                .orElse(userRepository.findByUsername(loginRequest.getEmail()).orElse(null));
        if (user == null) {
            return ResponseEntity.badRequest().body("user_not_found");
        }

        if (user.isTwoFactorEnabled()) {
            String token = jwtUtils.generateTokenFromEmailAndPassword(user.getEmail(), user.getPassword());
            return ResponseEntity.ok("jwt_two_factor: " + token);
        }

        return authenticate(user, loginRequest.getPassword());
    }

//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request, @RequestHeader(value = "host") String domainName) {
//        String sessionKey = request.getSession().getId();
//
//        return registrationService.handleRegistration(registerRequest, request, domainName, false);
//    }

    public ResponseEntity<UserInfoResponse> authenticate(User user, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail().toLowerCase(), password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        user.setAuthCount(user.getAuthCount() + 1);

        userRepository.save(user);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail().toLowerCase(),
                        roles));
    }
}
