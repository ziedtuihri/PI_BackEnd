package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.User;
import com.esprit.tn.pi.services.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@CrossOrigin(origins = "*")
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
