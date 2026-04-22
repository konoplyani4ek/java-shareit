package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}