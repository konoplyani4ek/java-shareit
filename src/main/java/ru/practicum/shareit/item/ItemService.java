package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;



    public ItemDto createItem(Long userId, ItemCreateDto itemCreateDto) {
        log.debug("Создание вещи для userId={}, name={}", userId, itemCreateDto.getName());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id=" + userId + " не найден"));

        ItemRequest request = null;
        if (itemCreateDto.getRequestId() != null) {
            request = itemRequestRepository.getById(itemCreateDto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Запрос с id=" + itemCreateDto.getRequestId() + " не найден"));
        }

        Item item = ItemMapper.toEntity(itemCreateDto, owner, request);
        Item savedItem = itemRepository.create(item);

        log.info("Создана вещь id={}, name={}, ownerId={}",
                savedItem.getId(), savedItem.getName(), userId);

        return ItemMapper.toDto(savedItem);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        log.debug("Обновление вещи itemId={} пользователем userId={}", itemId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        if (itemUpdateDto.getName() != null) existingItem.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null) existingItem.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) existingItem.setAvailable(itemUpdateDto.getAvailable());

        Item updatedItem = itemRepository.update(existingItem);

        log.info("Обновлена вещь id={}, userId={}", itemId, userId);

        return ItemMapper.toDto(updatedItem);
    }

    public ItemDto getItemById(Long itemId) {

        log.debug("Получение вещи по  itemId={}", itemId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));

        return ItemMapper.toDto(existingItem);
    }

    public List<ItemDto> getItemsByOwnerId(Long userId) {
        log.debug("Получение вещей пользователя userId={}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        List<Item> items = itemRepository.findByOwnerId(userId);

        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());

        return itemDtos;
    }

    public List<ItemDto> findAvailableItems(String nameOrDescription) {
        log.debug("Поиск вещей по тексту nameOrDescription={}", nameOrDescription);

        if (nameOrDescription == null || nameOrDescription.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.searchByText(nameOrDescription);

        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    public CommentDto createComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        log.debug("Попытка оставить комментарий userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));

        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, Status.APPROVED, LocalDateTime.now())) {
            throw new IllegalArgumentException("Комментарий нельзя оставить заранее");
        }

        Comment comment = CommentMapper.toEntity(commentCreateDto, existingItem, user);
        Comment saved = commentRepository.save(comment);

        log.info("Добавлен комментарий id={}, itemId={}, authorId={}", saved.getId(), itemId, userId);

        return CommentMapper.toDto(saved);
    }
}
