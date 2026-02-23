package ru.practicum.shareit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.InMemoryItemRepository;
import ru.practicum.shareit.request.repository.InMemoryItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ItemControllerTest {

    private ItemController itemController;
    private ItemService itemService;
    private InMemoryItemRepository itemRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryItemRequestRepository itemRequestRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        itemRepository = new InMemoryItemRepository();
        userRepository = new InMemoryUserRepository();
        itemRequestRepository = new InMemoryItemRequestRepository();
        itemService = new ItemService(itemRepository, userRepository, itemRequestRepository);
        itemController = new ItemController(itemService);

        owner = userRepository.create(User.builder()
                .name("Владелец")
                .email("owner@mail.ru")
                .build());

        otherUser = userRepository.create(User.builder()
                .name("Другой")
                .email("other@mail.ru")
                .build());
    }

    private ItemDto makeItemDto(String name, String description, Boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    @Nested
    @DisplayName("createItem")
    class CreateItem {

        @Test
        @DisplayName("успешное создание вещи")
        void shouldCreateItem() {
            ItemDto result = itemController.createItem(owner.getId(),
                    makeItemDto("Дрель", "Простая дрель", true));

            assertNotNull(result.getId());
            assertEquals("Дрель", result.getName());
            assertEquals("Простая дрель", result.getDescription());
            assertTrue(result.getAvailable());
        }

        @Test
        @DisplayName("создание недоступной вещи")
        void shouldCreateUnavailableItem() {
            ItemDto result = itemController.createItem(owner.getId(),
                    makeItemDto("Пила", "Ручная пила", false));

            assertFalse(result.getAvailable());
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemController.createItem(999L,
                            makeItemDto("Дрель", "Простая дрель", true)));
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        private Long itemId;

        @BeforeEach
        void createTestItem() {
            itemId = itemController.createItem(owner.getId(),
                    makeItemDto("Дрель", "Простая дрель", true)).getId();
        }

        @Test
        @DisplayName("обновление всех полей")
        void shouldUpdateAllFields() {
            ItemDto result = itemController.updateItem(owner.getId(), itemId,
                    makeItemDto("Перфоратор", "Мощный", false));

            assertEquals(itemId, result.getId());
            assertEquals("Перфоратор", result.getName());
            assertEquals("Мощный", result.getDescription());
            assertFalse(result.getAvailable());
        }

        @Test
        @DisplayName("частичное обновление — только name")
        void shouldUpdateOnlyName() {
            ItemDto result = itemController.updateItem(owner.getId(), itemId,
                    ItemDto.builder().name("Новое имя").build());

            assertEquals("Новое имя", result.getName());
            assertEquals("Простая дрель", result.getDescription());
            assertTrue(result.getAvailable());
        }

        @Test
        @DisplayName("частичное обновление — только description")
        void shouldUpdateOnlyDescription() {
            ItemDto result = itemController.updateItem(owner.getId(), itemId,
                    ItemDto.builder().description("Новое описание").build());

            assertEquals("Дрель", result.getName());
            assertEquals("Новое описание", result.getDescription());
        }

        @Test
        @DisplayName("частичное обновление — только available")
        void shouldUpdateOnlyAvailable() {
            ItemDto result = itemController.updateItem(owner.getId(), itemId,
                    ItemDto.builder().available(false).build());

            assertFalse(result.getAvailable());
            assertEquals("Дрель", result.getName());
        }

        @Test
        @DisplayName("ошибка при обновлении чужой вещи")
        void shouldThrowWhenNotOwner() {
            assertThrows(ForbiddenException.class,
                    () -> itemController.updateItem(otherUser.getId(), itemId,
                            makeItemDto("Хак", "Хак", true)));
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemController.updateItem(999L, itemId,
                            makeItemDto("Хак", "Хак", true)));
        }

        @Test
        @DisplayName("ошибка при несуществующей вещи")
        void shouldThrowWhenItemNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemController.updateItem(owner.getId(), 999L,
                            makeItemDto("Хак", "Хак", true)));
        }
    }

    @Nested
    @DisplayName("getItem")
    class GetItem {

        @Test
        @DisplayName("успешное получение вещи")
        void shouldReturnItem() {
            Long itemId = itemController.createItem(owner.getId(),
                    makeItemDto("Дрель", "Простая дрель", true)).getId();

            ItemDto result = itemController.getItem(itemId);

            assertEquals(itemId, result.getId());
            assertEquals("Дрель", result.getName());
        }

        @Test
        @DisplayName("ошибка при несуществующем id")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemController.getItem(999L));
        }
    }

    @Nested
    @DisplayName("getUserItems")
    class GetUserItems {

        @Test
        @DisplayName("возвращает вещи владельца")
        void shouldReturnOwnerItems() {
            itemController.createItem(owner.getId(), makeItemDto("Дрель", "Описание", true));
            itemController.createItem(owner.getId(), makeItemDto("Пила", "Описание", true));
            itemController.createItem(otherUser.getId(), makeItemDto("Чужая", "Описание", true));

            List<ItemDto> result = itemController.getUserItems(owner.getId());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("пустой список если нет вещей")
        void shouldReturnEmptyList() {
            List<ItemDto> result = itemController.getUserItems(owner.getId());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemController.getUserItems(999L));
        }
    }

    @Nested
    @DisplayName("searchItems")
    class SearchItems {

        @BeforeEach
        void createItems() {
            itemController.createItem(owner.getId(), makeItemDto("Дрель ударная", "Мощная дрель", true));
            itemController.createItem(owner.getId(), makeItemDto("Пила", "Для дерева", true));
            itemController.createItem(owner.getId(), makeItemDto("Отвёртка", "Крестовая дрель-шуруповёрт", false));
        }

        @Test
        @DisplayName("поиск по названию")
        void shouldFindByName() {
            List<ItemDto> result = itemController.searchItems("дрель");

            assertEquals(1, result.size());
            assertEquals("Дрель ударная", result.get(0).getName());
        }

        @Test
        @DisplayName("поиск по описанию")
        void shouldFindByDescription() {
            List<ItemDto> result = itemController.searchItems("дерев");

            assertEquals(1, result.size());
            assertEquals("Пила", result.get(0).getName());
        }

        @Test
        @DisplayName("не находит недоступные вещи")
        void shouldNotFindUnavailable() {
            List<ItemDto> result = itemController.searchItems("шуруповёрт");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("пустая строка — пустой результат")
        void shouldReturnEmptyForBlank() {
            assertTrue(itemController.searchItems("").isEmpty());
            assertTrue(itemController.searchItems("   ").isEmpty());
        }

        @Test
        @DisplayName("null — пустой результат")
        void shouldReturnEmptyForNull() {
            assertTrue(itemController.searchItems(null).isEmpty());
        }

        @Test
        @DisplayName("нет совпадений — пустой результат")
        void shouldReturnEmptyWhenNoMatch() {
            assertTrue(itemController.searchItems("самокат").isEmpty());
        }
    }
}