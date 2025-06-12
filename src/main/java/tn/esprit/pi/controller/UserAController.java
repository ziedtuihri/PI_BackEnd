package tn.esprit.pi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import tn.esprit.pi.dto.UserNameDto;
import tn.esprit.pi.services.IUserService;
import tn.esprit.pi.user.User;

import java.util.List;

public class UserAController {

    private  IUserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }



    @GetMapping("/role")
    public List<UserNameDto> getUsersByRole() {
        return userService.findUsersByRoles();
    }

}
