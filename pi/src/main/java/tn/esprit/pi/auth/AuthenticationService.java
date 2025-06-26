package tn.esprit.pi.auth;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import tn.esprit.pi.email.EmailService;
import tn.esprit.pi.email.EmailTemplateName;
import tn.esprit.pi.role.Role;
import tn.esprit.pi.role.RoleRepository;
import tn.esprit.pi.security.JwtService;
import tn.esprit.pi.user.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    private final String activationUrl = "http://localhost:4200/authentication/activate-account";

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName(request.getRole())
                // todo - better exception handling
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));

        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    public String resetPassword(ForgotRequest request) throws MessagingException {
        Optional<User> userDetails = userRepository.findByEmail(request.getEmail());

        if(userDetails.isEmpty()) {
            return "Invalid email";
        }

        if(!userDetails.get().getEnabled()){
            return "Activate your account";
        }

        User user = userDetails.get();

        String codeReset = generateCodeReset(user);

        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.FORGOT_PASSWORD,
                activationUrl + "?token=" + codeReset,
                codeReset,
                "Reset Code"
        );

        return "Code sent";
    }

    public String validateCodeReset(String code, String email) throws MessagingException {

        Optional<ActivationCode> savedToken = tokenRepository.findByCodeNumber(code);

        if(savedToken.isEmpty()){
            return "Invalid code";
        }

        if (LocalDateTime.now().isAfter(savedToken.get().getExpiresAt())) {
            return("Activation token has expired");
        }

        Optional<User> userDetails = userRepository.findByEmail(email);

        Optional<ActivationCode> codeReset = tokenRepository.findByCodeNumberAndUser(code, userDetails);

        if(String.valueOf(codeReset.get().getCodeNumber()).equals(code) && codeReset.get().getTypeCode().name().equals("PASSWORD_RESET_CODE")) {
            return "Code correct";
        }

        return "Invalid code";
    }

    public String changePassword(ChangePasswordRequest changePasswordRequest) throws MessagingException{

        Optional<ActivationCode> savedToken = tokenRepository.findByCodeNumber(changePasswordRequest.getCode());

        if(savedToken.isEmpty()){
            return "Invalid code";
        }

        if (LocalDateTime.now().isAfter(savedToken.get().getExpiresAt())) {
            return ("Activation code has expired");
        }

        Optional<User> userDetails = userRepository.findByEmail(changePasswordRequest.getEmail());
        Optional<ActivationCode> codeReset = tokenRepository.findByCodeNumberAndUser(changePasswordRequest.getCode(), userDetails);

        if(String.valueOf(codeReset.get().getCodeNumber()).equals(changePasswordRequest.getCode()) && codeReset.get().getTypeCode().name().equals("PASSWORD_RESET_CODE")) {
            User user = userDetails.get();

            user.setPassword(passwordEncoder.encode(changePasswordRequest.getPassword()));
            
            userRepository.save(user);
            return "password changed";
        }


        return "Error changing password";
    }

    public AuthenticationResponse handleFacebookLogin(RegistrationOptRequest request) {

        Optional<User> userDetails = userRepository.findByEmail(request.getEmail());

        var userRole = roleRepository.findByName("NULL");
        // var userRole = roleRepository.findByName(request.getRole());

        System.out.println("FNAME: ::::" + request.getFirstname());
        System.out.println("Lname: ::::" + request.getLastname());

        if(userDetails.isEmpty()) {

            var user = User.builder()
                    .firstName(request.getFirstname())
                    .lastName(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode("NULL"))
                    .accountLocked(false)
                    .enabled(true)
                    .roles(List.of(userRole.orElseThrow(() -> new IllegalStateException("ROLE NULL was not initiated"))))
                    .build();

            userRepository.save(user);

            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            "NULL"
                    )
            );

            var claims = new HashMap<String, Object>();
            var user2 = ((User) auth.getPrincipal());
            claims.put("fullName", user2.fullName());

            var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }

        if (userDetails.isPresent() && passwordEncoder.matches("NULL", userDetails.get().getPassword())) {
            // User exists, ask them to log in via email/password

            System.out.println("FIND  ***** ");

            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            "NULL"
                    )
            );

            var claims = new HashMap<String, Object>();
            var user = ((User) auth.getPrincipal());
            claims.put("fullName", user.fullName());

            var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }

        if (userDetails.isPresent() && !(userDetails.get().getPassword().equals("NULL"))) {
            return new AuthenticationResponse("Login with email and password");
        }

        // Check if the user exists in the database
        /*


         */
        return new AuthenticationResponse("Invalid email or password");

    }

    public AuthenticationResponse handleGoogleLogin(RegistrationOptRequest request) {

        Optional<User> userDetails = userRepository.findByEmail(request.getEmail());

        var userRole = roleRepository.findByName("NULL");
       // var userRole = roleRepository.findByName(request.getRole());

        if(userDetails.isEmpty()) {

            var user = User.builder()
                    .firstName(request.getFirstname())
                    .lastName(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode("NULL"))
                    .accountLocked(false)
                    .enabled(true)
                    .roles(List.of(userRole.orElseThrow(() -> new IllegalStateException("ROLE NULL was not initiated"))))
                    .build();

            userRepository.save(user);

            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            "NULL"
                    )
            );

            var claims = new HashMap<String, Object>();
            var user2 = ((User) auth.getPrincipal());
            claims.put("fullName", user2.fullName());

            var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();


        }

        if (userDetails.isPresent() && passwordEncoder.matches("NULL", userDetails.get().getPassword())) {
            // User exists, ask them to log in via email/password

            System.out.println("FIND  ***** ");

            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            "NULL"
                    )
            );

            var claims = new HashMap<String, Object>();
            var user = ((User) auth.getPrincipal());
            claims.put("fullName", user.fullName());

            var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();


        }

        if (userDetails.isPresent() && !(userDetails.get().getPassword().equals("NULL"))) {
            return new AuthenticationResponse("Login with email and password");
        }

        // Check if the user exists in the database
        /*


        */
        return new AuthenticationResponse("Invalid email or password");

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        Optional<User> userDetails = userRepository.findByEmail(request.getEmail());


        if(userDetails.isEmpty()) {
            return new AuthenticationResponse("Invalid email or password");
        }

        boolean isMatch = passwordEncoder.matches(request.getPassword(), userDetails.get().getPassword());

        if(!isMatch) {
            return new AuthenticationResponse("Invalid email or password");
        }

        // Check if the user is enabled
        if (!userDetails.get().getEnabled()) {
            return new AuthenticationResponse("Invalid account!");
        }

        if (!userDetails.get().getEnabled()) {
            return new AuthenticationResponse("Invalid account!");
        }

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.fullName());

        var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public void activateAccount(String activationCode) throws MessagingException {
        ActivationCode savedToken = tokenRepository.findByCodeNumber(activationCode)
                // todo exception has to be defined
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    private String generateCodeReset(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        var activationCode = ActivationCode.builder()
                .codeNumber(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .typeCode(CodeType.PASSWORD_RESET_CODE)
                .user(user)
                .build();
        tokenRepository.save(activationCode);

        return generatedToken;
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        var activationCode = ActivationCode.builder()
                .codeNumber(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .typeCode(CodeType.ACTIVATION_CODE)
                .user(user)
                .build();
        tokenRepository.save(activationCode);

        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl + "?token=" + newToken,
                newToken,
                "Account activation"
        );
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    RoleResponse checkUserRole(RoleRequest request) {

        List<Role> roles = roleRepository.findRolesByUserEmail(request.getEmail());

        String roleName = roles.stream()
                .findFirst()
                .map(Role::getName)
                .orElse("NO_ROLE");

        System.out.println("+++++++++ "+roleName);

        return new RoleResponse(roleName);
    }

    RoleResponse changeUserRole(RoleRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new RoleResponse("USER_NOT_FOUND");
        }

        Optional<Role> newRoleOpt = roleRepository.findByName(request.getRoleName());
        if (newRoleOpt.isEmpty()) {
            return new RoleResponse("ROLE_NOT_FOUND");
        }

        userRepository.removeAllRolesFromUser(userOpt.get().getId());

        userRepository.addRoleToUser(userOpt.get().getId(), newRoleOpt.get().getId());

        List<Role> roles = roleRepository.findRolesByUserEmail(request.getEmail());

        String roleName = roles.stream()
                .findFirst()
                .map(Role::getName)
                .orElse("NO_ROLE");

        if(roleName.equals(request.getRoleName())) {
            return new RoleResponse("ROLE_ASSIGNED");
        }

        return new RoleResponse("ROLE_NOT_ASSIGNED");
    }

}
