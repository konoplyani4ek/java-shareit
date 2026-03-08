package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepositoryInterface {

    private final Map<Long, Item> itemMap = new HashMap<>();
    private Long counter = 1L; // id

    @Override
    public Item create(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item не может быть null");
        }
        item.setId(generateId());
        itemMap.put(item.getId(), item);
        log.debug("Создан item с id={}, name={}", item.getId(), item.getName());
        return item;
    }

    @Override
    public Item update(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item не может быть null");
        }
        if (item.getId() == null) {
            throw new IllegalArgumentException("ID вещи не может быть null при обновлении");
        }

        Item existingItem = itemMap.get(item.getId());
        if (existingItem == null) {
            throw new NoSuchElementException("Item с id=" + item.getId() + " не найден");
        }

        itemMap.put(item.getId(), item);
        log.debug("Обновлён item с id={}, name={}", item.getId(), item.getName());
        return item;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Item removed = itemMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("Item с id=" + id + " не найден");
        }

        log.info("Удалён item с id={}, name={}", id, removed.getName());
    }

    @Override
    public Optional<Item> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemMap.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(itemMap.values());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return Collections.emptyList();
        }

        return itemMap.values().stream()
                .filter(item -> ownerId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();

        return itemMap.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(lowerText) ||
                                item.getDescription().toLowerCase().contains(lowerText)
                )
                .collect(Collectors.toList());
    }

    private long generateId() {
        return counter++;
    }
}