package drjik.bootcamp.controller;

import drjik.bootcamp.DTO.LoginRequest;
import drjik.bootcamp.DTO.RegisterRequest;
import drjik.bootcamp.security.JwtCore;
import drjik.bootcamp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "The Authentication API")
public class SecurityController {
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;
    private final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @PostMapping("/signup")
    @Operation(summary = "Register User", description = "Register user using email, username and password")
    ResponseEntity<?> signup(@RequestBody RegisterRequest registerRequest) {
        logger.info("Попытка на регистрацию пользователем с email: {}", registerRequest.getEmail());

        if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
            logger.warn("Этот email уже занят: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This email is already taken");
        }

        if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
            logger.warn("Этот username уже занят: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This username is already taken");
        }

        try {
            userService.saveUser(registerRequest);
            logger.info("Пользователь успешно зарегистрирован: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("Произошла ошибка при регистрации пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    @PostMapping("/signin")
    @Operation(summary = "Login user", description = "Login user using email and password")
    ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        logger.info("Попытка на аутентификацию пользователем с email: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    ));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            logger.info("Пользователь успешно авторизован: {}", loginRequest.getEmail());
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            logger.warn("Не удалось выполнить аутентификацию пользователя: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            logger.error("Произошла ошибка во время аутентификации: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication");
        }
    }
}
