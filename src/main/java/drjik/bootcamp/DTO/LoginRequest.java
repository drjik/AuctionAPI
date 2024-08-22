package drjik.bootcamp.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию")
public class LoginRequest {
    @Schema(description = "Адрес электронной почты", example = "bob@gmail.com")
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Email адрес должен быть в формате user@example.com")
    private String email;
    @Schema(description = "Пароль", example = "password123")
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
