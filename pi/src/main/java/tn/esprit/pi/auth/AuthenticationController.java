package tn.esprit.pi.auth;

import jakarta.mail.MessagingException;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
