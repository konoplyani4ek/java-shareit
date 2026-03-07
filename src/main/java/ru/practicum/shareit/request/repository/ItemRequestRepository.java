package ru.practicum.shareit.request.repository;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository {

    ItemRequest create(ItemRequest itemRequest);

    ItemRequest update(ItemRequest itemRequest);

    void deleteById(Long id);

    Optional<ItemRequest> getById(Long id);

    List<ItemRequest> findAll();

    List<ItemRequest> findByRequestorUserId(Long requestorId);

}