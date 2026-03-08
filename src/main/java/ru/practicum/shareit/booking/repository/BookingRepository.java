package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByOwnerUserId(@Param("ownerId") Long ownerId);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findByBookerIdOrderByStartDateTimeDesc(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.startDateTime <= :now AND b.endDateTime >= :now ORDER BY b.startDateTime DESC")
    List<Booking> findCurrentByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.endDateTime < :now ORDER BY b.startDateTime DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.startDateTime > :now ORDER BY b.startDateTime DESC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    List<Booking> findByBookerIdAndStatusOrderByStartDateTimeDesc(Long bookerId, Status status);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.startDateTime <= :now AND b.endDateTime >= :now ORDER BY b.startDateTime DESC")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.endDateTime < :now ORDER BY b.startDateTime DESC")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.startDateTime > :now ORDER BY b.startDateTime DESC")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status ORDER BY b.startDateTime DESC")
    List<Booking> findByOwnerIdAndStatusOrderByStartDateTimeDesc(@Param("ownerId") Long ownerId, @Param("status") Status status);

    boolean existsByBookerIdAndItemIdAndStatusAndEndDateTimeBefore(Long bookerId, Long itemId, Status status, LocalDateTime end); // конец был раньше чем сейчас
}