package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.createUser(
                new UserDto(null, "Owner", "owner@test.com"));
        ownerId = owner.getId();
    }

    @Test
    void createAndGetItem() {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная", true, null);

        ItemDto created = itemService.createItem(ownerId, dto);

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals("Мощная", created.getDescription());
        assertTrue(created.getAvailable());

        ItemDto found = itemService.getItemById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void updateItem_whenOwner_updatesSuccessfully() {
        ItemDto created = itemService.createItem(ownerId,
                new ItemCreateDto("Дрель", "Мощная", true, null));

        ItemDto updated = itemService.updateItem(ownerId, created.getId(),
                new ItemUpdateDto("Дрель Pro", null, false));

        assertEquals("Дрель Pro", updated.getName());
        assertEquals("Мощная", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void updateItem_whenNotOwner_throwsForbidden() {
        UserDto other = userService.createUser(
                new UserDto(null, "Other", "other@test.com"));
        ItemDto created = itemService.createItem(ownerId,
                new ItemCreateDto("Дрель", "Мощная", true, null));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(other.getId(), created.getId(),
                        new ItemUpdateDto("Хак", null, null)));
    }

    @Test
    void getItemsByOwnerId_returnsOnlyOwnerItems() {
        UserDto other = userService.createUser(
                new UserDto(null, "Other", "other@test.com"));

        itemService.createItem(ownerId, new ItemCreateDto("Дрель", "Мощная", true, null));
        itemService.createItem(ownerId, new ItemCreateDto("Пила", "Острая", true, null));
        itemService.createItem(other.getId(), new ItemCreateDto("Молоток", "Тяжёлый", true, null));

        List<ItemDto> items = itemService.getItemsByOwnerId(ownerId);

        assertEquals(2, items.size());
    }

    @Test
    void findAvailableItems_returnsMatchingItems() {
        itemService.createItem(ownerId, new ItemCreateDto("Дрель", "Мощная дрель", true, null));
        itemService.createItem(ownerId, new ItemCreateDto("Пила", "Острая пила", true, null));
        itemService.createItem(ownerId, new ItemCreateDto("Молоток", "Тяжёлый", false, null));

        List<ItemDto> result = itemService.findAvailableItems("дрель");

        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void getItemById_whenNotFound_throwsException() {
        assertThrows(NoSuchElementException.class,
                () -> itemService.getItemById(999L));
    }
}