package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;
    private ItemInfo item;   // item.id + item.name
    private UserInfo booker; // booker.id

    @Data
    @Builder
    public static class ItemInfo {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
    }
}
