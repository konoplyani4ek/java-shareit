package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
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

    @Transactional
    public ItemDto createItem(Long userId, ItemCreateDto dto) {
        log.debug("Создание вещи для userId={}", userId);
        User owner = getUserOrThrow(userId);
        ItemRequest request = null;
        if (dto.getRequestId() != null) {
            request = itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Запрос с id=" + dto.getRequestId() + " не найден"));
        }
        Item saved = itemRepository.save(ItemMapper.toEntity(dto, owner, request));
        log.info("Создана вещь id={}, ownerId={}", saved.getId(), userId);
        return ItemMapper.toDto(saved);
    }

    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto dto) {
        log.debug("Обновление вещи itemId={} пользователем userId={}", itemId, userId);
        getUserOrThrow(userId);
        Item existing = getItemOrThrow(itemId);
        if (!existing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());
        log.info("Обновлена вещь id={}", itemId);
        return ItemMapper.toDto(itemRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(Long itemId) {
        log.debug("Получение вещи itemId={}", itemId);
        return ItemMapper.toDto(getItemOrThrow(itemId));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        log.debug("Получение вещей пользователя userId={}", userId);
        getUserOrThrow(userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAvailableItems(String text) {
        log.debug("Поиск вещей по тексту: {}", text);
        if (text == null || text.isBlank()) return Collections.emptyList();
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentCreateDto dto) {
        log.debug("Добавление комментария userId={}, itemId={}", userId, itemId);
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateTimeBefore(
                userId, itemId, Status.APPROVED, LocalDateTime.now())) {
            throw new IllegalArgumentException("Комментарий можно оставить только после завершённого бронирования");
        }
        Comment saved = commentRepository.save(CommentMapper.toEntity(dto, item, user));
        log.info("Добавлен комментарий id={}, itemId={}, authorId={}", saved.getId(), itemId, userId);
        return CommentMapper.toDto(saved);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));
    }
}