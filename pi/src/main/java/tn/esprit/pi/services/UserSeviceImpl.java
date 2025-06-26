package tn.esprit.pi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.dto.UserNameDto;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSeviceImpl implements IUserService{
    private final UserRepository userRepository;


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /*@Override
    public List<User> findUsersByRoles() {
        return userRepository.findUsersByRoles();
    }*/

    @Override
    public List<UserNameDto> findUsersByRoles() {
        return userRepository.findUsersByRoles()
                .stream()
                .map(user -> new UserNameDto(user.getFirstName(), user.getLastName(), user.getId()))
                .toList();
    }

}
