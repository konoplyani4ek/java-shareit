package ru.practicum.shareit.booking.repository;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<Long, Booking> bookingMap = new HashMap<>();
    private Long counter = 1L; // id

    @Override
    public Booking create(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking не может быть null");
        }
        booking.setId(generateId());
        bookingMap.put(booking.getId(), booking);
        log.debug("Создан booking с id={}, itemId={}, bookerId={}",
                booking.getId(),
                booking.getItem().getId(),
                booking.getBooker().getId());
        return booking;
    }

    @Override
    public Booking update(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking не может быть null");
        }
        if (booking.getId() == null) {
            throw new IllegalArgumentException("ID бронирования не может быть null при обновлении");
        }

        Booking existingBooking = bookingMap.get(booking.getId());
        if (existingBooking == null) {
            throw new NoSuchElementException("Booking с id=" + booking.getId() + " не найден");
        }

        bookingMap.put(booking.getId(), booking);
        log.debug("Обновлён booking с id={}, status={}", booking.getId(), booking.getStatus());
        return booking;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Booking removed = bookingMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("Booking с id=" + id + " не найден");
        }

        log.info("Удалён booking с id={}", id);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(bookingMap.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookingMap.values());
    }

    @Override
    public List<Booking> findByBookerUserId(Long bookerId) {
        if (bookerId == null) {
            return Collections.emptyList();
        }

        return bookingMap.values().stream()
                .filter(booking -> bookerId.equals(booking.getBooker().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByOwnerUserId(Long ownerId) {
        if (ownerId == null) {
            return Collections.emptyList();
        }

        return bookingMap.values().stream()
                .filter(booking -> ownerId.equals(booking.getItem().getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemId(Long itemId) {
        if (itemId == null) {
            return Collections.emptyList();
        }

        return bookingMap.values().stream()
                .filter(booking -> itemId.equals(booking.getItem().getId()))
                .collect(Collectors.toList());
    }

    // затычки
    @Override
    public List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findPastByBookerId(Long bookerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findFutureByBookerId(Long bookerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findByBookerUserIdAndStatus(Long bookerId, Status status) {
        return List.of();
    }

    @Override
    public List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now) {
        return List.of();
    }

    @Override
    public List<Booking> findByOwnerUserIdAndStatus(Long ownerId, Status status) {
        return List.of();
    }

    @Override
    public boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, Status status, LocalDateTime end) {
        return false;
    }

    private long generateId() {
        return counter++;
    }
}