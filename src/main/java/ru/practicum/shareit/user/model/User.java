package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Builder.Default
    private Long id = null;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Email(message = "Некорректный формат email")
    @NotBlank
    private String email; // проверить на уникальность
}
