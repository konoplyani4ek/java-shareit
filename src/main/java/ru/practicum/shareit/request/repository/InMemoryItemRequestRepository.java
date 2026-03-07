package ru.practicum.shareit.request.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {

    private final Map<Long, ItemRequest> requestMap = new HashMap<>();
    private Long counter = 1L; // id

    @Override
    public ItemRequest create(ItemRequest itemRequest) {
        if (itemRequest == null) {
            throw new IllegalArgumentException("ItemRequest не может быть null");
        }
        itemRequest.setId(generateId());
        requestMap.put(itemRequest.getId(), itemRequest);
        log.debug("Создан itemRequest с id={}, requestorId={}, description={}",
                itemRequest.getId(),
                itemRequest.getRequestor().getId(),
                itemRequest.getDescription());
        return itemRequest;
    }

    @Override
    public ItemRequest update(ItemRequest itemRequest) {
        if (itemRequest == null) {
            throw new IllegalArgumentException("ItemRequest не может быть null");
        }
        if (itemRequest.getId() == null) {
            throw new IllegalArgumentException("ID запроса не может быть null при обновлении");
        }

        ItemRequest existingRequest = requestMap.get(itemRequest.getId());
        if (existingRequest == null) {
            throw new NoSuchElementException("ItemRequest с id=" + itemRequest.getId() + " не найден");
        }

        requestMap.put(itemRequest.getId(), itemRequest);
        log.debug("Обновлён itemRequest с id={}", itemRequest.getId());
        return itemRequest;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        ItemRequest removed = requestMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("ItemRequest с id=" + id + " не найден");
        }

        log.info("Удалён itemRequest с id={}", id);
    }

    @Override
    public Optional<ItemRequest> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(requestMap.get(id));
    }

    @Override
    public List<ItemRequest> findAll() {
        return new ArrayList<>(requestMap.values());
    }

    @Override
    public List<ItemRequest> findByRequestorUserId(Long requestorId) {
        if (requestorId == null) {
            return Collections.emptyList();
        }

        return requestMap.values().stream()
                .filter(request -> requestorId.equals(request.getRequestor().getId()))
                .collect(Collectors.toList());
    }

    private long generateId() {
        return counter++;
    }
}