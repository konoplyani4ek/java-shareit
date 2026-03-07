package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class DbItemRepository implements ItemRepository {

    private final ItemJpaRepository jpa;

    @Override
    public Item create(Item item) {
        return jpa.save(item);
    }

    @Override
    public Item update(Item item) {
        return jpa.save(item);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Item> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return jpa.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return jpa.searchByText(text);
    }
}