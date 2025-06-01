package tn.esprit.pi.auth;

import jakarta.mail.MessagingException;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;


@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@TableGenerator(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World! from AUTHENTICATION SERVICE";
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/authenticateOption")
    public ResponseEntity<AuthenticationResponse> authenticateOption(
            @RequestBody RegistrationOptRequest request
    ) {
        return ResponseEntity.ok(service.handleGoogleLogin(request));
    }



    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        service.activateAccount(token);
    }


    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestBody ForgotRequest email) throws MessagingException {
        return service.resetPassword(email);
    }


    @GetMapping("/resetPassword")
    public String resetPassword(
            @RequestParam String codeReset, String email
    )throws MessagingException {
        String response = service.validateCodeReset(codeReset, email);
        return response;
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) throws MessagingException {
        String response = service.changePassword(changePasswordRequest);
        return response;
    }

    @PostMapping("/checkUserRole")
    public RoleResponse checkUserRole(
            @RequestBody RoleRequest request
    ) throws MessagingException {
        RoleResponse response = service.checkUserRole(request);
        return response;
    }

    @PostMapping("/changeUserRole")
    public RoleResponse changeUserRole(
            @RequestBody RoleRequest request
    ) throws MessagingException {
        RoleResponse response = service.changeUserRole(request);
        return response;
    }


    @GetMapping("/oauth2/callback/facebook")
    public String facebookCallback(OAuth2AuthenticationToken token) {
        OAuth2User user = token.getPrincipal();
        String email = user.getAttribute("email");
        return "Logged in with Facebook: " + email;
    }

}
