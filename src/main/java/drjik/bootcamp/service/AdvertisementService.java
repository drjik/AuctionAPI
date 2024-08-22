package drjik.bootcamp.service;

import drjik.bootcamp.DTO.AdvertisementRequest;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.AdvertisementRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdvertisementService {
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementService.class);
    private final AdvertisementRepository advertisementRepository;

    public Advertisement saveAdvertisement(AdvertisementRequest advertisementRequest) {
        logger.info("Начало сохранения объявления: {}", advertisementRequest.getTitle());

        Advertisement advertisement = new Advertisement();

        advertisement.setUser(advertisementRequest.getUser());
        advertisement.setTitle(advertisementRequest.getTitle());
        advertisement.setDescription(advertisementRequest.getDescription());
        advertisement.setStartingPrice(advertisementRequest.getStartingPrice());
        advertisement.setCurrentPrice(advertisementRequest.getStartingPrice());
        advertisement.setActive(advertisementRequest.isActive());
        advertisement.setImage(advertisementRequest.getImageURL());

        try {
            Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
            logger.info("Объявление успешно сохранено с ID: {}", savedAdvertisement.getId());
            return savedAdvertisement;
        } catch (Exception e) {
            logger.error("Ошибка при сохранении объявления: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Advertisement> getAdvertisementByUser(User user) {
        logger.info("Начало получения объявления с email: {}", user.getEmail());

        try {
            List<Advertisement> advertisements = advertisementRepository.findByUser(user);
            logger.info("Найдено {} объявлений для пользователя: {}", advertisements.size(), user.getEmail());
            return advertisements;
        } catch (Exception e) {
            logger.error("Ошибка при поиске объявлений для пользователя: {}", user.getEmail(), e);
            throw e;
        }
    }

    public List<Advertisement> getActiveAdvertisements(String title, Double minPrice, Double maxPrice) {
        logger.info("Начало поиска активных объявлений с фильтрацией: title='{}', minPrice={}, maxPrice={}", title, minPrice, maxPrice);

        try {
            List<Advertisement> advertisements = advertisementRepository.findActiveAdvertisements(title, minPrice, maxPrice);
            logger.info("Найдено {} активных объявлений", advertisements.size());
            return advertisements;
        } catch (Exception e) {
            logger.error("Ошибка при поиске активных объявлений с фильтрацией: title='{}', minPrice={}, maxPrice{}", title, minPrice, maxPrice, e);
            throw e;
        }
    }

    public void removeAdvertisement(Long id) {
        logger.info("Попытка отключения объявления с id={}", id);

        try {
            Advertisement advertisement = advertisementRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Объявления с id={} не найдено", id);
                        return new IllegalArgumentException("Объявления не найдено");
                    });
            advertisement.setActive(false);
            advertisementRepository.save(advertisement);
            logger.info("Объявление с id={} успешно отключена", id);
        } catch (Exception e) {
            logger.error("Ошибка при отключении объявления с id={}", id);
            throw e;
        }
    }
}
