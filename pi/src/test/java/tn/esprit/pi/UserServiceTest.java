package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAjouterUser_ShouldReturnSavedUser() {
        User user = User.builder()
                .id(1)
                .firstName("Meriem")
                .lastName("Ben Hajji")
                .email("meriem@example.com")
                .password("password123")
                .enabled(true)
                .accountLocked(false)
                .dateOfBirth(LocalDate.of(1990, 5, 20))
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.ajouterUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(1);
        assertThat(savedUser.getEmail()).isEqualTo("meriem@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("Meriem");
        assertThat(savedUser.getLastName()).isEqualTo("Ben Hajji");
        assertThat(savedUser.getEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void testGetAllUsers_ShouldReturnList() {
        User user1 = User.builder()
                .id(1)
                .email("meriem@example.com")
                .firstName("Meriem")
                .lastName("Ben Hajji")
                .build();

        User user2 = User.builder()
                .id(2)
                .email("nour@example.com")
                .firstName("Nour")
                .lastName("Tissaoui")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getEmail()).isEqualTo("meriem@example.com");
        assertThat(users.get(1).getEmail()).isEqualTo("nour@example.com");
        verify(userRepository).findAll();
    }
}