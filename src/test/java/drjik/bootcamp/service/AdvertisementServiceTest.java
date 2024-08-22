package drjik.bootcamp.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import drjik.bootcamp.DTO.AdvertisementRequest;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.AdvertisementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdvertisementServiceTest {
    @Mock
    private AdvertisementRepository advertisementRepository;
    @InjectMocks
    private AdvertisementService advertisementService;

    private final ListAppender<ILoggingEvent> listAppender = setupLogger();

    @Test
    void testSaveAdvertisementSuccess() {
        AdvertisementRequest request = mock(AdvertisementRequest.class);
        Advertisement advertisement = mock(Advertisement.class);

        when(request.getUser()).thenReturn(mock(User.class));
        when(request.getTitle()).thenReturn("Test Title");
        when(request.getDescription()).thenReturn("Test Description");
        when(request.getStartingPrice()).thenReturn(100.0);
        when(request.isActive()).thenReturn(true);
        when(request.getImageURL()).thenReturn("test-image-url");

        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(advertisement);
        when(advertisement.getId()).thenReturn(1L);


        Advertisement savedAdvertisement = advertisementService.saveAdvertisement(request);

        assertNotNull(savedAdvertisement);
        assertEquals(1L, savedAdvertisement.getId());

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Начало сохранения объявления: Test Title",
                "Объявление успешно сохранено с ID: 1");
    }

    @Test
    void testGetAdvertisementByUser() {
        Advertisement advertisement = mock(Advertisement.class);
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findByUser(user)).thenReturn(List.of(advertisement));

        List<Advertisement> result = advertisementService.getAdvertisementByUser(user);

        assertEquals(1, result.size());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Начало получения объявления с email: test@gmail.com",
                "Найдено 1 объявлений для пользователя: test@gmail.com");
    }

    @Test
    void testGetAdvertisementByUserNoAdvertisements() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findByUser(user)).thenReturn(Collections.emptyList());

        List<Advertisement> result = advertisementService.getAdvertisementByUser(user);

        assertEquals(0, result.size());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Начало получения объявления с email: test@gmail.com",
                "Найдено 0 объявлений для пользователя: test@gmail.com");
    }

    @Test
    void testGetActiveAdvertisements() {
        Advertisement advertisement = mock(Advertisement.class);

        String title = "title";
        Double minPrice = 100.0;
        Double maxPrice = 500.0;

        when(advertisementRepository.findActiveAdvertisements(title, minPrice, maxPrice)).thenReturn(List.of(advertisement));

        List<Advertisement> result = advertisementService.getActiveAdvertisements(title, minPrice, maxPrice);

        assertEquals(1, result.size());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Начало поиска активных объявлений с фильтрацией: title='title', minPrice=100.0, maxPrice=500.0",
                "Найдено 1 активных объявлений");
    }

    @Test
    void testGetActiveAdvertisementsNoAdvertisements() {
        String title = "title";
        Double minPrice = 100.0;
        Double maxPrice = 100.0;

        when(advertisementRepository.findActiveAdvertisements(title, minPrice, maxPrice)).thenReturn(Collections.emptyList());

        List<Advertisement> result = advertisementService.getActiveAdvertisements(title, minPrice, maxPrice);

        assertEquals(0, result.size());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Начало поиска активных объявлений с фильтрацией: title='title', minPrice=100.0, maxPrice=100.0",
                "Найдено 0 активных объявлений");
    }

    @Test
    void testRemoveAdvertisement() {
        Advertisement advertisement = mock(Advertisement.class);
        Long advertisementId = 1L;

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        advertisementService.removeAdvertisement(advertisementId);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Попытка отключения объявления с id=1",
                "Объявление с id=1 успешно отключена");
        verify(advertisementRepository).findById(advertisementId);
        verify(advertisement).setActive(false);
        verify(advertisementRepository).save(advertisement);
    }

    private ListAppender<ILoggingEvent> setupLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setContext(loggerContext);
        listAppender.start();
        loggerContext.getLogger(AdvertisementService.class).addAppender(listAppender);
        return listAppender;
    }

    private void verifyLogs(List<ILoggingEvent> logsList, String... messages) {
        for (String message : messages) {
            assertTrue(logsList.stream().anyMatch(e -> e.getFormattedMessage().contains(message)),
                    "Expected log message not found: " + message);
        }
    }
}
