package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingService bookingService;

    private final User owner = new User(1L, "Owner", "owner@test.com");
    private final User booker = new User(2L, "Booker", "booker@test.com");
    private final Item item = Item.builder()
            .id(1L).name("Дрель").available(true).owner(owner).build();

    @Test
    void createBooking_whenValid_returnsDto() {
        BookingCreateDto dto = new BookingCreateDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        Booking saved = Booking.builder()
                .id(1L).item(item).booker(booker)
                .startDateTime(dto.getStart()).endDateTime(dto.getEnd())
                .status(Status.WAITING).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(saved);

        BookingDto result = bookingService.createBooking(2L, dto);

        assertEquals(1L, result.getId());
        assertEquals(Status.WAITING, result.getStatus());
    }

    @Test
    void createBooking_whenItemNotAvailable_throwsException() {
        Item unavailable = Item.builder()
                .id(1L).available(false).owner(owner).build();
        BookingCreateDto dto = new BookingCreateDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(unavailable));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(2L, dto));
    }

    @Test
    void approveBooking_whenOwner_approvesSuccessfully() {
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertEquals(Status.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_whenNotOwner_throwsForbidden() {
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(2L, 1L, true));
    }

    @Test
    void getBookingInfo_whenStranger_throwsForbidden() {
        User stranger = new User(3L, "Stranger", "stranger@test.com");
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(stranger));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.getBookingInfo(3L, 1L));
    }

    @Test
    void getBookingInfo_whenBooker_returnsDto() {
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingInfo(2L, 1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getUserBookings_whenStateAll_returnsAll() {
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(2L)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(2L, BookingState.ALL);

        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenStateWaiting_returnsWaiting() {
        Booking booking = Booking.builder()
                .id(1L).item(item).booker(booker).status(Status.WAITING).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByOwnerIdAndStatusOrderByStartDateTimeDesc(1L, Status.WAITING))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getOwnerBookings(1L, BookingState.WAITING);

        assertEquals(1, result.size());
        assertEquals(Status.WAITING, result.get(0).getStatus());
    }
}