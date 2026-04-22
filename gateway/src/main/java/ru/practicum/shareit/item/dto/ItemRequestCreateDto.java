package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestCreateDto {
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
}