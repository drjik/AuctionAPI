package drjik.bootcamp.repository;

import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByUser(User user);
    @Query("SELECT a FROM Advertisement a WHERE a.active = true AND " +
            "(COALESCE(:title, '') = '' OR a.title LIKE %:title%) AND " +
            "(:minPrice IS NULL OR a.currentPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR a.currentPrice <= :maxPrice)")
    List<Advertisement> findActiveAdvertisements(
            @Param("title") String title,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
    List<Advertisement> findAllByActiveTrueAndAuctionEndTimeBefore(LocalDateTime time);
}
