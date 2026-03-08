package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        log.info("POST /items - создание вещи пользователем userId={}", userId);
        return itemService.createItem(userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        log.info("PATCH /items/{} - обновление вещи пользователем userId={}", itemId, userId);
        return itemService.updateItem(userId, itemId, itemUpdateDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        log.info("GET /items/{} - просмотр вещи", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items - получение вещей пользователя userId={}", userId);
        return itemService.getItemsByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(required = false) String text) {
        log.info("GET /items/search - поиск вещей по тексту: {}", text);
        return itemService.findAvailableItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentCreateDto commentCreateDto
    ) {
        log.info("POST /items/{}/comment - добавление комментария userId={}", itemId, userId);
        return itemService.createComment(userId, itemId, commentCreateDto);
    }


}