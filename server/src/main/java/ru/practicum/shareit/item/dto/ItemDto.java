package ru.practicum.shareit.item.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    @Builder.Default
    private Object lastBooking = null;
    @Builder.Default
    private Object nextBooking = null;
    private List<CommentDto> comments;
}