package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepositoryInterface {

    Booking create(Booking booking);

    Booking update(Booking booking);

    void deleteById(Long id);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    List<Booking> findByBookerUserId(Long bookerId);

    List<Booking> findByOwnerUserId(Long ownerId);

    List<Booking> findByItemId(Long itemId);

}