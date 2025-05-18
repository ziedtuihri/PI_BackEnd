package tn.esprit.pi.restcontrollers;

import tn.esprit.pi.services.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.user.User;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)

@RequestMapping("/pi/users")
public class UserController {
    private final UserService UserService;

    @PostMapping
    public ResponseEntity<User> ajouterUtilisateur(@RequestBody User user) {
        User createdUser = UserService.ajouterUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(UserService.getAllUsers(), HttpStatus.OK);
    }
}
