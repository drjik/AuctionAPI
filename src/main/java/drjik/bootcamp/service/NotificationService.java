package drjik.bootcamp.service;

import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@AllArgsConstructor
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void notifyPreviousBidder(Advertisement advertisement, User user) {
        if (advertisement.getBids().isEmpty()) {
            return;
        }

        advertisement.getBids().stream()
                .sorted(Comparator.comparing(Bid::getTimestamp).reversed())
                .filter(bid -> !bid.getUser().equals(user))
                .findFirst().ifPresent(previousBid -> logger.info("Message sent to email: {}, Your bid has been outbid", previousBid.getUser().getEmail()));

    }

    public void notifySeller(Advertisement advertisement) {
        logger.info("Message sent to email: {}, New bid on your advertisement", advertisement.getUser().getEmail());
    }

    public void notifyWinner(User user) {
        logger.info("Message sent to email: {}, Congratulations, you won the auction!", user.getEmail());
    }

    public void notifySellerEnd(Advertisement advertisement) {
        logger.info("Message sent to email: {}, Auction ended, your auction has ended successfully)", advertisement.getUser().getEmail());
    }
}
