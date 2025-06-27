package tn.esprit.pi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User ajouterUser(User u) {
        if (u == null) {
            throw new IllegalArgumentException("User ne peut pas être nul");
        }
        return userRepository.save(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
