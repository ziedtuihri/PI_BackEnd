package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.User;
import com.esprit.tn.pi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User ajouterUser(User u) {
        return userRepository.save(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
