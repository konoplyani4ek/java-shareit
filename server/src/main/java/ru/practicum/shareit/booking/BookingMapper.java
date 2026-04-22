package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDateTime())
                .end(booking.getEndDateTime())
                .status(booking.getStatus())
                .item(BookingDto.ItemInfo.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .booker(BookingDto.UserInfo.builder()
                        .id(booking.getBooker().getId())
                        .build())
                .build();
    }

    public static Booking toEntity(BookingCreateDto dto, Item item, User booker) {
        return Booking.builder()
                .startDateTime(dto.getStart())
                .endDateTime(dto.getEnd())
                .item(item)
                .booker(booker)
                .build();
    }
}