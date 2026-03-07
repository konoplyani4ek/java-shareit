package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking create(Booking booking);

    Booking update(Booking booking);

    void deleteById(Long id);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    List<Booking> findByBookerUserId(Long bookerId);

    List<Booking> findByOwnerUserId(Long ownerId);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now);

    List<Booking> findPastByBookerId(Long bookerId, LocalDateTime now);

    List<Booking> findFutureByBookerId(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerUserIdAndStatus(Long bookerId, Status status);

    List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now);

    List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now);

    List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now);

    List<Booking> findByOwnerUserIdAndStatus(Long ownerId, Status status);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, Status status, LocalDateTime end);
}