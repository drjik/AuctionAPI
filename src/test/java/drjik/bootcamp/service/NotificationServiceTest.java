package drjik.bootcamp.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testNotifyPreviousBidderNoBids() {
        Advertisement advertisement = mock(Advertisement.class);
        User user = mock(User.class);

        when(advertisement.getBids()).thenReturn(Collections.emptyList());

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifyPreviousBidder(advertisement, user);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList);
    }

    @Test
    void testNotifyPreviousBidderWithBidsSendsNotification() {
        Advertisement advertisement = mock(Advertisement.class);
        User currentUser = mock(User.class);
        User previousUser = mock(User.class);
        Bid previousBid = mock(Bid.class);
        Bid currentBid = mock(Bid.class);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusMinutes(1);

        when(previousBid.getTimestamp()).thenReturn(earlier);
        when(currentBid.getTimestamp()).thenReturn(now);
        when(previousBid.getUser()).thenReturn(previousUser);
        when(currentBid.getUser()).thenReturn(currentUser);
        when(advertisement.getBids()).thenReturn(List.of(previousBid, currentBid));
        when(previousUser.getEmail()).thenReturn("test@gmail.com");

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifyPreviousBidder(advertisement, currentUser);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Message sent to email: test@gmail.com, Your bid has been outbid");
    }

    @Test
    void testNotifyPreviousBidderWithBidsNoNotification() {
        Advertisement advertisement = mock(Advertisement.class);
        User user = mock(User.class);
        Bid previousBid = mock(Bid.class);
        Bid currentBid = mock(Bid.class);

        LocalDateTime now = LocalDateTime.now();

        when(previousBid.getTimestamp()).thenReturn(now);
        when(currentBid.getTimestamp()).thenReturn(now);
        when(previousBid.getUser()).thenReturn(user);
        when(currentBid.getUser()).thenReturn(user);
        when(advertisement.getBids()).thenReturn(List.of(previousBid, currentBid));

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifyPreviousBidder(advertisement, user);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList);
    }

    @Test
    void testNotifySeller() {
        Advertisement advertisement = mock(Advertisement.class);
        User user = mock(User.class);
        when(advertisement.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("test@gmail.com");

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifySeller(advertisement);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Message sent to email: test@gmail.com, New bid on your advertisement");
    }

    @Test
    void testNotifyWinner() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@gmail.com");

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifyWinner(user);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Message sent to email: test@gmail.com, Congratulations, you won the auction!");
    }

    @Test
    void testNotifySellerEnd() {
        Advertisement advertisement = mock(Advertisement.class);
        User user = mock(User.class);
        when(advertisement.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("test@gmail.com");

        ListAppender<ILoggingEvent> listAppender = setupLogger();

        notificationService.notifySellerEnd(advertisement);

        List<ILoggingEvent> logsList = listAppender.list;
        verifyLogs(logsList,
                "Message sent to email: test@gmail.com, Auction ended, your auction has ended successfully)");
    }

    private ListAppender<ILoggingEvent> setupLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setContext(loggerContext);
        listAppender.start();
        loggerContext.getLogger(NotificationService.class).addAppender(listAppender);
        return listAppender;
    }

    private void verifyLogs(List<ILoggingEvent> logsList, String... messages) {
        for (String message : messages) {
            assertTrue(logsList.stream().anyMatch(e -> e.getFormattedMessage().contains(message)),
                    "Expected log message not found: " + message);
        }
    }
}
