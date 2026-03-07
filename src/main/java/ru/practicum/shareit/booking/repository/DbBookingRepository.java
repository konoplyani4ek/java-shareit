package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class DbBookingRepository implements BookingRepository {

    private final BookingJpaRepository jpa;

    @Override
    public Booking create(Booking booking) {
        return jpa.save(booking);
    }

    @Override
    public Booking update(Booking booking) {
        return jpa.save(booking);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Booking> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Booking> findByBookerUserId(Long bookerId) {
        return jpa.findByBookerId(bookerId);
    }

    @Override
    public List<Booking> findByOwnerUserId(Long ownerId) {
        return jpa.findByOwnerUserId(ownerId);
    }

    @Override
    public List<Booking> findByItemId(Long itemId) {
        return jpa.findByItemId(itemId);
    }

    @Override
    public List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now) {
        return jpa.findCurrentByBookerId(bookerId, now);
    }

    @Override
    public List<Booking> findPastByBookerId(Long bookerId, LocalDateTime now) {
        return jpa.findPastByBookerId(bookerId, now);
    }

    @Override
    public List<Booking> findFutureByBookerId(Long bookerId, LocalDateTime now) {
        return jpa.findFutureByBookerId(bookerId, now);
    }

    @Override
    public List<Booking> findByBookerUserIdAndStatus(Long bookerId, Status status) {
        return jpa.findByBookerIdAndStatusOrderByStartDesc(bookerId, status);
    }

    @Override
    public List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now) {
        return jpa.findCurrentByOwnerId(ownerId, now);
    }

    @Override
    public List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now) {
        return jpa.findPastByOwnerId(ownerId, now);
    }

    @Override
    public List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now) {
        return jpa.findFutureByOwnerId(ownerId, now);
    }

    @Override
    public List<Booking> findByOwnerUserIdAndStatus(Long ownerId, Status status) {
        return jpa.findByOwnerIdAndStatusOrderByStartDesc(ownerId, status);
    }

    @Override
    public boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, Status status, LocalDateTime end) {
        return jpa.existsByBookerIdAndItemIdAndStatusAndEndBefore(bookerId, itemId, status, end);
    }
}