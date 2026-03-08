package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto
    ) {
        return bookingService.createBooking(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingDto approveBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {

        // временный
        log.warn("CONTROLLER CALLED");
        log.debug("Запрос PATCH /bookings/{}?approved={} от пользователя userId={}",
                bookingId, approved, userId);

        BookingDto result = bookingService.approveBooking(userId, bookingId, approved);

        log.info("Букинг id={} обработан пользователем userId={}, новый статус={}",
                bookingId, userId, result.getStatus());

        return result;
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingInfo(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getBookingInfo(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        return bookingService.getOwnerBookings(userId, state);
    }
}
