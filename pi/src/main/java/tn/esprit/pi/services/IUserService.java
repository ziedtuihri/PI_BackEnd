package tn.esprit.pi.services;

import tn.esprit.pi.dto.UserNameDto;
import tn.esprit.pi.user.User;

import java.util.List;

public interface IUserService {
     List<User> getAllUsers();

    // List<User> findUsersByRoles();

     List<UserNameDto> findUsersByRoles();




}
