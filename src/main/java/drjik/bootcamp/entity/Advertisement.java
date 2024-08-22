package drjik.bootcamp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "advertisements")
@Getter
@Setter
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private boolean active;
    private String image;
    private LocalDateTime auctionEndTime;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL)
    private List<Bid> bids;
}
