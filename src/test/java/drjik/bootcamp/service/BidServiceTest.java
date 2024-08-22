package drjik.bootcamp.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.AdvertisementRepository;
import drjik.bootcamp.repository.BidRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {
    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private BidService bidService;

    @Test
    void testPlaceBidSuccess() {
        User user = mock(User.class);
        Advertisement advertisement = mock(Advertisement.class);
        Bid bid = mock(Bid.class);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisement.isActive()).thenReturn(true);
        when(advertisement.getCurrentPrice()).thenReturn(100.0);
        when(advertisement.getAuctionEndTime()).thenReturn(null);

        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        Bid resultBid = bidService.placeBid(user, 1L, 200.0);

        assertNotNull(resultBid);
        verify(advertisementRepository).findById(1L);
        verify(advertisementRepository, times(2)).save(advertisement);
        verify(bidRepository).save(any(Bid.class));
        verify(notificationService).notifyPreviousBidder(advertisement, user);
        verify(notificationService).notifySeller(advertisement);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Пользователь test@gmail.com делает ставку на объявление с ID: 1, с суммой: 200.0",
                "Ставка на сумму 200.0 успешно размещена пользователем test@gmail.com");
    }

    @Test
    void testPlaceBidInactiveAuction() {
        User user = mock(User.class);
        Advertisement advertisement = mock(Advertisement.class);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisement.isActive()).thenReturn(false);

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bidService.placeBid(user, 1L, 200.0));

        assertEquals("The auction is not active", exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Попытка пользователя test@gmail.com сделать ставку на неактивный аукцион с ID: 1");
    }

    @Test
    void testPlaceBidAmountTooLow() {
        User user = mock(User.class);
        Advertisement advertisement = mock(Advertisement.class);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisement.isActive()).thenReturn(true);
        when(advertisement.getCurrentPrice()).thenReturn(100.0);

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bidService.placeBid(user, 1L, 50.0));

        assertEquals("Bid amount must be higher than the starting price, Current price - 100.0", exception.getMessage());
        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Попытка пользователя test@gmail.com сделать ставку на сумму 50.0 меньшей или равной текущей цене 100.0");
    }

    @Test
    void testPlaceBidWithExistingEndTime() {
        User user = mock(User.class);
        Advertisement advertisement = mock(Advertisement.class);
        Bid bid = mock(Bid.class);
        LocalDateTime auctionEndTime = LocalDateTime.now().plusMinutes(5);

        when(user.getEmail()).thenReturn("test@gmail.com");
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisement.isActive()).thenReturn(true);
        when(advertisement.getCurrentPrice()).thenReturn(100.0);
        when(advertisement.getAuctionEndTime()).thenReturn(auctionEndTime);

        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        Bid resultBid = bidService.placeBid(user, 1L, 200.0);

        assertNotNull(resultBid);
        verify(advertisementRepository).findById(1L);
        verify(advertisementRepository, times(1)).save(advertisement);
        verify(bidRepository).save(any(Bid.class));
        verify(notificationService).notifyPreviousBidder(advertisement, user);
        verify(notificationService).notifySeller(advertisement);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Пользователь test@gmail.com делает ставку на объявление с ID: 1, с суммой: 200.0",
                "Ставка на сумму 200.0 успешно размещена пользователем test@gmail.com");
    }

    private ListAppender<ILoggingEvent> setupLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setContext(loggerContext);
        listAppender.start();
        loggerContext.getLogger(BidService.class).addAppender(listAppender);
        return listAppender;
    }

    private void verifyLogs(List<ILoggingEvent> logsList, String... messages) {
        for (String message : messages) {
            assertTrue(logsList.stream().anyMatch(e -> e.getFormattedMessage().contains(message)),
                    "Expected log message not found: " + message);
        }
    }
}
