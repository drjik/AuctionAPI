package drjik.bootcamp.controller;

import drjik.bootcamp.DTO.AdvertisementRequest;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.security.UserDetailsImpl;
import drjik.bootcamp.service.AdvertisementService;
import drjik.bootcamp.service.BidService;
import drjik.bootcamp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/advertisements")
@Tag(name = "Advertisement", description = "The Advertisement API")
@AllArgsConstructor
public class AdvertisementController {
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementController.class);
    private AdvertisementService advertisementService;
    private UserService userService;
    private BidService bidService;

    @PostMapping("/create")
    @Operation(summary = "Create a new advertisement")
    public ResponseEntity<?> createAdvertisement(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody AdvertisementRequest advertisementRequest) {
        Optional<User> user = userService.findByEmail(userDetails.getUsername());
        advertisementRequest.setUser(user.get());
        advertisementRequest.setActive(true);

        logger.info("Получен запрос на создание объявления с заголовком: {}, пользователем: {}", advertisementRequest.getTitle(), user.get().getEmail());
        try {
            Advertisement advertisement = advertisementService.saveAdvertisement(advertisementRequest);
            return ResponseEntity.ok(advertisement);
        } catch (Exception e) {
            logger.error("Ошибка при создании объявления: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating advertisement");
        }
    }

    @GetMapping("/user")
    @Operation(summary = "Get advertisements By User")
    public ResponseEntity<?> getUserAdvertisements(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Получен запрос на получение объявлений пользователя с email: {}", userDetails.getUsername());

        Optional<User> user = userService.findByEmail(userDetails.getUsername());

        try {
            List<Advertisement> advertisements = advertisementService.getAdvertisementByUser(user.get());
            return ResponseEntity.ok(advertisements);
        } catch (Exception e) {
            logger.error("Ошибка при получении объявлений пользователя с email: {} : {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обработке запроса");
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get active advertisements", description = "You can filter the request by title, minPrice and maxPrice parameters")
    public ResponseEntity<?> getActiveAdvertisements(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        logger.info("Получен запрос на получение активных объявлений");

        try {
            List<Advertisement> advertisements = advertisementService.getActiveAdvertisements(title, minPrice, maxPrice);
            return ResponseEntity.ok(advertisements);
        } catch (Exception e) {
            logger.error("Ошибка при получении активных объявлений: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обработке запроса");
        }
    }

    @PostMapping("/remove/{id}")
    @Operation(summary = "Remove advertisement by id")
    public ResponseEntity<?> removeAdvertisement(@PathVariable Long id) {
        logger.info("Получен запрос на отключение объявления с ID: {}", id);

        try {
            advertisementService.removeAdvertisement(id);
            return ResponseEntity.ok("Advertisement removed");
        } catch (Exception e) {
            logger.error("Ошибка при отключении объявления с ID: {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обработке запроса");
        }
    }

    @PostMapping("/bid/{id}")
    @Operation(summary = "Bid advertisement by id")
    public ResponseEntity<?> placeBid(@PathVariable Long id, @RequestParam Double amount, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Запрос на размещение ставки от пользователя с email: {} для объявления с ID: {} на сумму: {}", userDetails.getUsername(), id, amount);

        Optional<User> user = userService.findByEmail(userDetails.getUsername());
        try {
            Bid bid = bidService.placeBid(user.get(), id, amount);
            return ResponseEntity.ok("bid placed successfully: " + bid.getAmount());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при размещении ставки пользователем с email {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
