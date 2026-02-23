package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.debug("Создание вещи для userId={}, name={}", userId, itemDto.getName());

        // Проверяем, что пользователь существует
        User owner = userRepository.getById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id=" + userId + " не найден"));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.getById(itemDto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Запрос с id=" + itemDto.getRequestId() + " не найден"));
        }

        //  DTO в Entity
        Item item = ItemMapper.toEntity(itemDto, owner, request); // добавление от владельца

        Item savedItem = itemRepository.create(item);

        log.info("Создана вещь id={}, name={}, ownerId={}",
                savedItem.getId(), savedItem.getName(), userId);

        return ItemMapper.toDto(savedItem);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Обновление вещи itemId={} пользователем userId={}", itemId, userId);

        // существование пользователя
        User user = userRepository.getById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));

        Item existingItem = itemRepository.getById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Редактировать вещь может только её владелец");
        }

        // частичное обновление
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setIsAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(existingItem);

        log.info("Обновлена вещь id={}, userId={}", itemId, userId);

        return ItemMapper.toDto(updatedItem);
    }

    public ItemDto getItemById(Long itemId) {

        log.debug("Получение вещи по  itemId={}", itemId);
        Item existingItem = itemRepository.getById(itemId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Вещь с id=" + itemId + " не найдена"));

        return ItemMapper.toDto(existingItem);
    }

    public List<ItemDto> getItemsByOwnerId(Long userId) {
        log.debug("Получение вещей пользователя userId={}", userId);
        userRepository.getById(userId)
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
}
