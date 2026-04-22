// server/src/test/java/ru/practicum/shareit/request/ItemRequestServiceTest.java
package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock private ItemRequestRepository itemRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestService itemRequestService;

    private final User user = new User(1L, "User", "user@test.com");

    @Test
    void createRequest_whenValid_returnsDto() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequest saved = ItemRequest.builder()
                .id(1L).description("Нужна дрель")
                .requestor(user).created(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(saved);

        ItemRequestDto result = itemRequestService.createRequest(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void createRequest_whenUserNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.createRequest(99L, new ItemRequestCreateDto("Дрель")));
    }

    @Test
    void getUserRequests_returnsRequestsWithItems() {
        ItemRequest request = ItemRequest.builder()
                .id(1L).description("Нужна дрель")
                .requestor(user).created(LocalDateTime.now()).build();
        Item item = Item.builder().id(1L).name("Дрель").owner(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void getAllRequests_excludesOwnRequests() {
        ItemRequest request = ItemRequest.builder()
                .id(1L).description("Нужна дрель")
                .requestor(new User(2L, "Other", "other@test.com"))
                .created(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getRequestById_whenNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getRequestById(1L, 99L));
    }

    @Test
    void getRequestById_whenFound_returnsDto() {
        ItemRequest request = ItemRequest.builder()
                .id(1L).description("Нужна дрель")
                .requestor(user).created(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of());

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
    }
}