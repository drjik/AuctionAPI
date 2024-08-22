package drjik.bootcamp.service;

import drjik.bootcamp.DTO.RegisterRequest;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public User saveUser(RegisterRequest registerRequest) {
        logger.info("Попытка создания пользователя с email: {}", registerRequest.getEmail());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        try {
            User savedUser = userRepository.save(user);
            logger.info("Пользователь успешно создан с ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.warn("Ошибка при сохранении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Optional<User> findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user;
        } else {
            logger.warn("Пользователь с email={} не найден", email);
            throw new IllegalArgumentException("Пользователь не найден");
        }
    }

    public Optional<User> findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user;
        } else {
            logger.warn("Пользователь с username={} не найден", username);
            throw new IllegalArgumentException("Пользователь не найден");
        }
    }
}
