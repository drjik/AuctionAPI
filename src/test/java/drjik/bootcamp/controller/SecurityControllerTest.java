package drjik.bootcamp.controller;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import drjik.bootcamp.DTO.LoginRequest;
import drjik.bootcamp.DTO.RegisterRequest;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.security.JwtCore;
import drjik.bootcamp.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SecurityControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtCore jwtCore;
    @Mock
    private UserService userService;
    @InjectMocks
    private SecurityController securityController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(securityController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSigninSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("123");
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        org.springframework.security.core.Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtCore.generateToken(authentication)).thenReturn("jwt-token");

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));

        verifyLogMessages(listAppender.list,
                "Попытка на аутентификацию пользователем с email: test@gmail.com",
                "Пользователь успешно авторизован: test@gmail.com");
    }

    @Test
    void testSigninInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("123");
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verifyLogMessages(listAppender.list,
                "Попытка на аутентификацию пользователем с email: test@gmail.com",
                "Не удалось выполнить аутентификацию пользователя: test@gmail.com");
    }

    @Test
    void testSignupSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setUsername("user");
        registerRequest.setPassword("123");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userService.findByUsername("user")).thenReturn(Optional.empty());

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verifyLogMessages(listAppender.list,
                "Попытка на регистрацию пользователем с email: test@gmail.com",
                "Пользователь успешно зарегистрирован: test@gmail.com");
    }

    @Test
    void testSignupEmailTaken() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setUsername("user");
        registerRequest.setPassword("123");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.of(mock(User.class)));

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This email is already taken"));

        verifyLogMessages(listAppender.list,
                "Этот email уже занят: test@gmail.com");
    }

    @Test
    void testSignupUsernameTaken() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setUsername("user");
        registerRequest.setPassword("123");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userService.findByUsername("user")).thenReturn(Optional.of(mock(User.class)));

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This username is already taken"));

        verifyLogMessages(listAppender.list,
                "Этот username уже занят: user");
    }

    @Test
    void testSignupError() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setUsername("user");
        registerRequest.setPassword("123");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);

        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        when(userService.findByUsername("user")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Registration error")).when(userService).saveUser(any(RegisterRequest.class));

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred during registration"));

        verifyLogMessages(listAppender.list,
                "Попытка на регистрацию пользователем с email: test@gmail.com",
                "Произошла ошибка при регистрации пользователя: Registration error");
    }

    private ListAppender<ILoggingEvent> setupLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setContext(loggerContext);
        listAppender.start();
        loggerContext.getLogger(SecurityController.class).addAppender(listAppender);
        return listAppender;
    }

    private void verifyLogMessages(List<ILoggingEvent> logsList, String... expectedMessages) {
        List<String> actualMessages = logsList.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        for (String expectedMessage : expectedMessages) {
            assertTrue(actualMessages.contains(expectedMessage), "Expected log message not found: " + expectedMessage);
        }
    }
}
