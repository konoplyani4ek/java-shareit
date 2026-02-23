package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Builder.Default
    private Long id = null;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean isAvailable;
    @NotNull
    private User owner;
    @Builder.Default
    private ItemRequest request = null; // если вещь была создана по запросу другого пользователя, то в этом поле будет храниться ссылка на соответствующий запрос.
}
