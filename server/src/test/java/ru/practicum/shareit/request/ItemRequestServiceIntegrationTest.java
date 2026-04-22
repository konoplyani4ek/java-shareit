// server/src/test/java/ru/practicum/shareit/request/ItemRequestServiceIntegrationTest.java
package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long requestorId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        UserDto requestor = userService.createUser(
                new UserDto(null, "Requestor", "requestor@test.com"));
        requestorId = requestor.getId();

        UserDto other = userService.createUser(
                new UserDto(null, "Other", "other@test.com"));
        otherUserId = other.getId();
    }

    @Test
    void createRequest_whenValid_returnsDto() {
        ItemRequestDto result = itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Нужна дрель"));

        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getUserRequests_returnsOwnRequestsWithItems() {
        ItemRequestDto request = itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Нужна дрель"));

        itemService.createItem(otherUserId,
                new ItemCreateDto("Дрель", "Мощная", true, request.getId()));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(requestorId);

        assertEquals(1, result.size());
        assertEquals("Нужна дрель", result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Дрель", result.get(0).getItems().get(0).getName());
    }

    @Test
    void getAllRequests_excludesOwnRequests() {
        itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Нужна дрель"));
        itemRequestService.createRequest(
                otherUserId, new ItemRequestCreateDto("Нужна пила"));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requestorId);

        assertEquals(1, result.size());
        assertEquals("Нужна пила", result.get(0).getDescription());
    }

    @Test
    void getRequestById_whenFound_returnsDto() {
        ItemRequestDto created = itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Нужна дрель"));

        ItemRequestDto result = itemRequestService.getRequestById(otherUserId, created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Нужна дрель", result.getDescription());
    }

    @Test
    void getRequestById_whenNotFound_throwsException() {
        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getRequestById(requestorId, 999L));
    }

    @Test
    void getUserRequests_sortedByCreatedDesc() {
        itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Первый запрос"));
        itemRequestService.createRequest(
                requestorId, new ItemRequestCreateDto("Второй запрос"));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(requestorId);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreated()
                .isAfter(result.get(1).getCreated()) ||
                result.get(0).getCreated()
                        .isEqual(result.get(1).getCreated()));
    }
}