package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item create(Item item);

    Item update(Item item);

    void deleteById(Long id);

    Optional<Item> getById(Long id);

    List<Item> findAll();

    List<Item> findByOwnerId(Long ownerId);

    List<Item> searchByText(String text);
}
