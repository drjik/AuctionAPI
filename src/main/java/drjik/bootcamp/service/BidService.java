package drjik.bootcamp.service;

import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.repository.AdvertisementRepository;
import drjik.bootcamp.repository.BidRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {
    @Value("${auction.time}")
    private int auctionTime;

    private final Logger logger = LoggerFactory.getLogger(BidService.class);
    private final BidRepository bidRepository;
    private final AdvertisementRepository advertisementRepository;
    private final NotificationService notificationService;



    @Transactional
    public synchronized Bid placeBid(User user, Long advertisementId, Double amount) {
        logger.info("Пользователь {} делает ставку на объявление с ID: {}, с суммой: {}", user.getEmail(), advertisementId, amount);

        Advertisement advertisement = advertisementRepository.findById(advertisementId).orElseThrow(() -> new IllegalArgumentException("Invalid advertisement id"));

        if (!advertisement.isActive()) {
            logger.warn("Попытка пользователя {} сделать ставку на неактивный аукцион с ID: {}", user.getEmail(), advertisementId);
            throw new IllegalArgumentException("The auction is not active");
        }

        if (amount <= advertisement.getCurrentPrice()) {
            logger.warn("Попытка пользователя {} сделать ставку на сумму {} меньшей или равной текущей цене {}",user.getEmail(), amount, advertisement.getCurrentPrice());
            throw new IllegalArgumentException("Bid amount must be higher than the starting price, Current price - " + advertisement.getCurrentPrice());
        }

        if (advertisement.getAuctionEndTime() == null) {
            advertisement.setAuctionEndTime(LocalDateTime.now().plusMinutes(auctionTime));
            advertisementRepository.save(advertisement);
            logger.info("Время окончания аукциона с ID: {} установлено на {}", advertisementId, advertisement.getAuctionEndTime());
        }

        Bid bid = new Bid();

        bid.setAmount(amount);
        bid.setUser(user);
        bid.setAdvertisement(advertisement);
        bid.setTimestamp(LocalDateTime.now());
        bidRepository.save(bid);

        advertisement.setCurrentPrice(amount);
        advertisementRepository.save(advertisement);

        notificationService.notifyPreviousBidder(advertisement, user);
        notificationService.notifySeller(advertisement);

        logger.info("Ставка на сумму {} успешно размещена пользователем {}", amount, user.getEmail());

        return bid;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void checkAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Advertisement> activeAdvertisements = advertisementRepository.findAllByActiveTrueAndAuctionEndTimeBefore(now);

        for (Advertisement advertisement : activeAdvertisements) {
            advertisement.setActive(false);
            advertisementRepository.save(advertisement);

            notificationService.notifySellerEnd(advertisement);
            advertisement.getBids().stream()
                    .max(Comparator.comparing(Bid::getAmount))
                    .ifPresent(lastBid -> notificationService.notifyWinner(lastBid.getUser()));

        }
    }

}
