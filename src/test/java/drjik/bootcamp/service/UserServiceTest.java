package drjik.bootcamp.service;

import drjik.bootcamp.DTO.RegisterRequest;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void testFindByEmail() {
        String email = "test@gmail.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmail(email);

        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    void testFindByUsername() {
        String username = "username";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByUsername(username);

        assertEquals(username, foundUser.get().getUsername());
    }

    @Test
    void testSaveUser() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setUsername("username");
        registerRequest.setPassword("password");

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(registerRequest);

        assertEquals("test@gmail.com", savedUser.getEmail());
        assertEquals("username", savedUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
