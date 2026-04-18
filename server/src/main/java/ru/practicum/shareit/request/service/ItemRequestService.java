package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto dto) {
        log.debug("Создание запроса вещи userId={}", userId);
        User requestor = getUserOrThrow(userId);
        ItemRequest saved = itemRequestRepository.save(ItemRequestMapper.toEntity(dto, requestor));
        log.info("Создан запрос id={}, userId={}", saved.getId(), userId);
        return ItemRequestMapper.toDto(saved, List.of());
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.debug("Получение запросов пользователя userId={}", userId);
        getUserOrThrow(userId);
        return itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(r -> ItemRequestMapper.toDto(r, itemRepository.findByRequestId(r.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.debug("Получение всех чужих запросов для userId={}", userId);
        getUserOrThrow(userId);
        return itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(r -> ItemRequestMapper.toDto(r, itemRepository.findByRequestId(r.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.debug("Получение запроса id={} пользователем userId={}", requestId, userId);
        getUserOrThrow(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Запрос с id=" + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toDto(request, items);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с id=" + userId + " не найден"));
    }
}