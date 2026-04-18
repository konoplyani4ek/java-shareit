package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto dto) {
        log.debug("Создание букинга userId={}, itemId={}", userId, dto.getItemId());
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(dto.getItemId());
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        Booking saved = bookingRepository.save(BookingMapper.toEntity(dto, item, booker));
        log.info("Создан букинг id={}, itemId={}, bookerId={}", saved.getId(), item.getId(), userId);
        return BookingMapper.toDto(saved);
    }

    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.debug("Подтверждение букинга id={}, userId={}", bookingId, userId);
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        log.info("Букинг id={} -> статус={}", bookingId, booking.getStatus());
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingDto getBookingInfo(Long userId, Long bookingId) {
        log.debug("Получение букинга id={} пользователем userId={}", bookingId, userId);
        getUserOrThrow(userId);
        Booking booking = getBookingOrThrow(bookingId);
        if (!Objects.equals(userId, booking.getItem().getOwner().getId())
                && !Objects.equals(userId, booking.getBooker().getId())) {
            throw new ForbiddenException("Просмотр доступен только владельцу вещи или арендатору");
        }
        return BookingMapper.toDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        log.debug("Получение букингов пользователя userId={}, state={}", userId, state);
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now);
            case PAST -> bookingRepository.findPastByBookerId(userId, now);
            case FUTURE -> bookingRepository.findFutureByBookerId(userId, now);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDateTimeDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDateTimeDesc(userId, Status.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        log.debug("Получение букингов владельца userId={}, state={}", userId, state);
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByOwnerUserId(userId);
            case CURRENT -> bookingRepository.findCurrentByOwnerId(userId, now);
            case PAST -> bookingRepository.findPastByOwnerId(userId, now);
            case FUTURE -> bookingRepository.findFutureByOwnerId(userId, now);
            case WAITING -> bookingRepository.findByOwnerIdAndStatusOrderByStartDateTimeDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByOwnerIdAndStatusOrderByStartDateTimeDesc(userId, Status.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Букинг с id=" + bookingId + " не найден"));
    }
}