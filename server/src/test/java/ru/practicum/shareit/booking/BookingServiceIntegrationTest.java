package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.createUser(
                new UserDto(null, "Owner", "owner@test.com"));
        ownerId = owner.getId();

        UserDto booker = userService.createUser(
                new UserDto(null, "Booker", "booker@test.com"));
        bookerId = booker.getId();

        ItemDto item = itemService.createItem(ownerId,
                new ItemCreateDto("Дрель", "Мощная", true, null));
        itemId = item.getId();
    }

    @Test
    void createBooking_whenValid_returnsWaiting() {
        BookingCreateDto dto = new BookingCreateDto(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingDto result = bookingService.createBooking(bookerId, dto);

        assertNotNull(result.getId());
        assertEquals(Status.WAITING, result.getStatus());
        assertEquals(itemId, result.getItem().getId());
        assertEquals(bookerId, result.getBooker().getId());
    }

    @Test
    void approveBooking_whenOwner_approvesSuccessfully() {
        BookingDto booking = bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));

        BookingDto approved = bookingService.approveBooking(ownerId, booking.getId(), true);

        assertEquals(Status.APPROVED, approved.getStatus());
    }

    @Test
    void approveBooking_whenNotOwner_throwsForbidden() {
        BookingDto booking = bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(bookerId, booking.getId(), true));
    }

    @Test
    void getUserBookings_stateAll_returnsAll() {
        bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));

        List<BookingDto> result = bookingService.getUserBookings(bookerId, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_stateAll_returnsAll() {
        bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));

        List<BookingDto> result = bookingService.getOwnerBookings(ownerId, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingInfo_whenOwner_returnsDto() {
        BookingDto booking = bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));

        BookingDto result = bookingService.getBookingInfo(ownerId, booking.getId());

        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void createBooking_whenItemNotAvailable_throwsException() {
        bookingService.createBooking(bookerId,
                new BookingCreateDto(
                        itemId,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2)
                ));
        itemService.updateItem(ownerId, itemId,
                new ru.practicum.shareit.item.dto.ItemUpdateDto(null, null, false));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(bookerId,
                        new BookingCreateDto(
                                itemId,
                                LocalDateTime.now().plusDays(3),
                                LocalDateTime.now().plusDays(4)
                        )));
    }
}