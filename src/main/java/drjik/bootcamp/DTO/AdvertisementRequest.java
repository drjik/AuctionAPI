package drjik.bootcamp.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drjik.bootcamp.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Схема для создания товара")
public class AdvertisementRequest {
    @JsonIgnore
    private User user;
    @Schema(description = "Название товара")
    @NotBlank(message = "Название товара не может быть пустым")
    private String title;
    @Schema(description = "Описание товара")
    @NotBlank(message = "Описание товара не может быть пустым")
    private String description;
    @Schema(description = "Начальная цена")
    @NotBlank(message = "Начальная цена не может быть пустым")
    private double startingPrice;
    @JsonIgnore
    private boolean active;
    @Schema(description = "URL картинки")
    @NotBlank(message = "URL картинки не может быть пустым")
    private String imageURL;
}
