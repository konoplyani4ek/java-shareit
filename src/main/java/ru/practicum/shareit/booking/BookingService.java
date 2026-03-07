package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto) {
        log.debug("Создание нового букинга");
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь для букинга не найдена"));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        Booking booking = BookingMapper.toEntity(bookingCreateDto, item, booker);
        Booking saved = bookingRepository.create(booking);
        log.info("Создан букинг id={}, itemId={}, bookerId={}, status={}",
                saved.getId(), item.getId(), userId, saved.getStatus());

        return BookingMapper.toDto(saved);
    }

    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.warn("Подтверждение букинга id={}, Status={}, Владелец userId={}", bookingId, approved, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Букинг не найден"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("WRONG USER: bookingId={}, ownerId={}, userId={}",
                    bookingId, booking.getItem().getOwner().getId(), userId);
            throw new NoSuchElementException("Подтвердить бронирование может только владелец вещи");
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else booking.setStatus(Status.REJECTED);

        Booking saved = bookingRepository.update(booking);

        return BookingMapper.toDto(saved);
    }

    public BookingDto getBookingInfo(Long userId, Long bookingId) {
        log.debug("Пользователь id={} запросил статус букинга id={}", userId, bookingId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Букинг не найден"));

        if (!Objects.equals(userId, booking.getItem().getOwner().getId()) && !Objects.equals(userId, booking.getBooker().getId())) {
            throw new ForbiddenException("Проверить информацию может только владелец вещи или запроса");
        }

        return BookingMapper.toDto(booking);
    }

    public List<BookingDto> getUserBookings(Long userId, BookingState state) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerUserId(userId);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now);
            case PAST -> bookingRepository.findPastByBookerId(userId, now);
            case FUTURE -> bookingRepository.findFutureByBookerId(userId, now);
            case WAITING -> bookingRepository.findByBookerUserIdAndStatus(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBookerUserIdAndStatus(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByOwnerUserId(userId);
            case CURRENT -> bookingRepository.findCurrentByOwnerId(userId, now);
            case PAST -> bookingRepository.findPastByOwnerId(userId, now);
            case FUTURE -> bookingRepository.findFutureByOwnerId(userId, now);
            case WAITING -> bookingRepository.findByOwnerUserIdAndStatus(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByOwnerUserIdAndStatus(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }
}
