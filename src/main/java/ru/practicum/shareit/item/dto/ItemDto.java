package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Укажите доступность вещи")
    private Boolean available;
    private Long requestId;
    @Builder.Default
    private Object lastBooking = null;  // зачем в тестах?
    @Builder.Default
    private Object nextBooking = null;  //
    private List<CommentDto> comments;
}
