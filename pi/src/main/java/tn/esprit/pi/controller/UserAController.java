package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.UserNameDto;
import tn.esprit.pi.services.IUserService;
import tn.esprit.pi.user.User;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAController {

    private final IUserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/role")
    public List<UserNameDto> getUsersByRole() {
        return userService.findUsersByRoles();
    }
}
