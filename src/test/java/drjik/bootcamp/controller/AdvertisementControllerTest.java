package drjik.bootcamp.controller;

import drjik.bootcamp.DTO.AdvertisementRequest;
import drjik.bootcamp.entity.Advertisement;
import drjik.bootcamp.entity.Bid;
import drjik.bootcamp.entity.User;
import drjik.bootcamp.security.UserDetailsImpl;
import drjik.bootcamp.service.AdvertisementService;
import drjik.bootcamp.service.BidService;
import drjik.bootcamp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvertisementControllerTest{
    @Mock
    private AdvertisementService advertisementService;
    @Mock
    private UserService userService;
    @Mock
    private BidService bidService;
    @InjectMocks
    private AdvertisementController advertisementController;

    @Test
    public void testCreateAdvertisementSuccess() {
        User user = new User();
        user.setEmail("test@example.com");

        AdvertisementRequest adRequest = mock(AdvertisementRequest.class);
        Advertisement advertisement = new Advertisement();

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(advertisementService.saveAdvertisement(adRequest)).thenReturn(advertisement);

        ResponseEntity<?> response = advertisementController.createAdvertisement(userDetails, adRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(advertisement, response.getBody());
        verify(adRequest).setUser(user);
        verify(adRequest).setActive(true);
        verify(advertisementService).saveAdvertisement(adRequest);
    }

    @Test
    public void testCreateAdvertisementFailure() {
        User user = new User();
        user.setEmail("test@example.com");

        AdvertisementRequest adRequest = mock(AdvertisementRequest.class);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(advertisementService.saveAdvertisement(adRequest)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = advertisementController.createAdvertisement(userDetails, adRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error creating advertisement", response.getBody());
        verify(adRequest).setUser(user);
        verify(adRequest).setActive(true);
        verify(advertisementService).saveAdvertisement(adRequest);
    }

    @Test
    public void testGetUserAdvertisements() {
        User user = new User();
        user.setEmail("test@gmail.com");

        Advertisement ad1 = new Advertisement();
        Advertisement ad2 = new Advertisement();
        List<Advertisement> advertisements = Arrays.asList(ad1, ad2);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("test@gmail.com");
        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(advertisementService.getAdvertisementByUser(user)).thenReturn(advertisements);

        ResponseEntity<?> response = advertisementController.getUserAdvertisements(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(advertisements, response.getBody());
    }

    @Test
    public void testGetActiveAdvertisements() {
        Advertisement ad1 = new Advertisement();
        List<Advertisement> advertisements = List.of(ad1);

        when(advertisementService.getActiveAdvertisements("Test Title", 100.0, 500.0)).thenReturn(advertisements);

        ResponseEntity<?> response = advertisementController.getActiveAdvertisements("Test Title", 100.0, 500.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(advertisements, response.getBody());
        verify(advertisementService).getActiveAdvertisements("Test Title", 100.0, 500.0);
    }

    @Test
    public void testGetActiveAdvertisementsFailure() {
        when(advertisementService.getActiveAdvertisements(null, null, null)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = advertisementController.getActiveAdvertisements(null, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ошибка при обработке запроса", response.getBody());
        verify(advertisementService).getActiveAdvertisements(null, null, null);
    }

    @Test
    public void testRemoveAdvertisementSuccess() {
        Long id = 1L;

        doNothing().when(advertisementService).removeAdvertisement(id);

        ResponseEntity<?> response = advertisementController.removeAdvertisement(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Advertisement removed", response.getBody());
        verify(advertisementService).removeAdvertisement(id);
    }

    @Test
    public void testRemoveAdvertisementFailure() {
        Long id = 1L;

        doThrow(new RuntimeException("Database error")).when(advertisementService).removeAdvertisement(id);

        ResponseEntity<?> response = advertisementController.removeAdvertisement(id);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ошибка при обработке запроса", response.getBody());
        verify(advertisementService).removeAdvertisement(id);
    }

    @Test
    public void testPlaceBid() {
        Long id = 1L;
        Double amount = 100.0;
        User user = new User();
        Bid bid = new Bid();
        bid.setAmount(amount);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bidService.placeBid(user, id, amount)).thenReturn(bid);

        ResponseEntity<?> response = advertisementController.placeBid(id, amount, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("bid placed successfully: " + amount, response.getBody());
        verify(bidService).placeBid(user, id, amount);
    }

    @Test
    public void testPlaceBidInvalidArgument() {
        Long adId = 1L;
        Double amount = 100.0;
        User user = new User();

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bidService.placeBid(user, adId, amount)).thenThrow(new IllegalArgumentException("Invalid bid amount"));

        ResponseEntity<?> response = advertisementController.placeBid(adId, amount, userDetails);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid bid amount", response.getBody());
        verify(bidService).placeBid(user, adId, amount);
    }
}
