package tn.esprit.pi.auth;

import jakarta.mail.MessagingException;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.services.UserService;
import tn.esprit.pi.user.User;

import java.util.List;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@TableGenerator(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;
    private final UserService userService;

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



    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
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
            @RequestBody RoleRequest email
    ) throws MessagingException {
        RoleResponse response = service.checkUserRole(email);
        return response;
    }



}
