package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.InMemoryItemRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.InMemoryItemRequestRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    private ItemService itemService;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        itemRepository = new InMemoryItemRepository();
        itemRequestRepository = new InMemoryItemRequestRepository();
        itemService = new ItemService(itemRepository, userRepository, itemRequestRepository,
                Mockito.mock(BookingRepository.class), Mockito.mock(CommentRepository.class));

        owner = userRepository.create(User.builder()
                .name("Владелец")
                .email("owner@mail.ru")
                .build());

        otherUser = userRepository.create(User.builder()
                .name("Другой")
                .email("other@mail.ru")
                .build());
    }

    private ItemCreateDto makeItemDto(String name, String description, Boolean available) {
        return ItemCreateDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private ItemUpdateDto makeItemUpdateDto(String name, String description, Boolean available) {
        return ItemUpdateDto.builder()
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
            ItemCreateDto dto = makeItemDto("Дрель", "Простая дрель", true);

            ItemDto result = itemService.createItem(owner.getId(), dto);

            assertNotNull(result.getId());
            assertEquals("Дрель", result.getName());
            assertEquals("Простая дрель", result.getDescription());
            assertTrue(result.getAvailable());
            assertNull(result.getRequestId());
        }

        @Test
        @DisplayName("создание вещи с available=false")
        void shouldCreateUnavailableItem() {
            ItemCreateDto dto = makeItemDto("Отвёртка", "Крестовая", false);

            ItemDto result = itemService.createItem(owner.getId(), dto);

            assertFalse(result.getAvailable());
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            ItemCreateDto dto = makeItemDto("Дрель", "Простая дрель", true);

            assertThrows(NoSuchElementException.class,
                    () -> itemService.createItem(999L, dto));
        }

        @Test
        @DisplayName("ошибка при несуществующем requestId")
        void shouldThrowWhenRequestNotFound() {
            ItemCreateDto dto = makeItemDto("Дрель", "Простая дрель", true);
            dto.setRequestId(999L);

            assertThrows(NoSuchElementException.class,
                    () -> itemService.createItem(owner.getId(), dto));
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        private ItemDto createdItem;

        @BeforeEach
        void createTestItem() {
            createdItem = itemService.createItem(owner.getId(),
                    makeItemDto("Дрель", "Простая дрель", true));
        }

        @Test
        @DisplayName("обновление всех полей")
        void shouldUpdateAllFields() {
            ItemUpdateDto updateDto = makeItemUpdateDto("Перфоратор", "Мощный перфоратор", false);

            ItemDto result = itemService.updateItem(owner.getId(), createdItem.getId(), updateDto);

            assertEquals(createdItem.getId(), result.getId());
            assertEquals("Перфоратор", result.getName());
            assertEquals("Мощный перфоратор", result.getDescription());
            assertFalse(result.getAvailable());
        }

        @Test
        @DisplayName("частичное обновление — только name")
        void shouldUpdateOnlyName() {
            ItemUpdateDto updateDto = ItemUpdateDto.builder().name("Новое имя").build();

            ItemDto result = itemService.updateItem(owner.getId(), createdItem.getId(), updateDto);

            assertEquals("Новое имя", result.getName());
            assertEquals("Простая дрель", result.getDescription());
            assertTrue(result.getAvailable());
        }

        @Test
        @DisplayName("частичное обновление — только description")
        void shouldUpdateOnlyDescription() {
            ItemUpdateDto updateDto = ItemUpdateDto.builder().description("Новое описание").build();

            ItemDto result = itemService.updateItem(owner.getId(), createdItem.getId(), updateDto);

            assertEquals("Дрель", result.getName());
            assertEquals("Новое описание", result.getDescription());
        }

        @Test
        @DisplayName("частичное обновление — только available")
        void shouldUpdateOnlyAvailable() {
            ItemUpdateDto updateDto = ItemUpdateDto.builder().available(false).build();

            ItemDto result = itemService.updateItem(owner.getId(), createdItem.getId(), updateDto);

            assertFalse(result.getAvailable());
            assertEquals("Дрель", result.getName());
        }

        @Test
        @DisplayName("ошибка при обновлении чужой вещи")
        void shouldThrowWhenNotOwner() {
            ItemUpdateDto updateDto = makeItemUpdateDto("Хак", "Хак", true);

            assertThrows(ForbiddenException.class,
                    () -> itemService.updateItem(otherUser.getId(), createdItem.getId(), updateDto));
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            ItemUpdateDto updateDto = makeItemUpdateDto("Хак", "Хак", true);

            assertThrows(NoSuchElementException.class,
                    () -> itemService.updateItem(999L, createdItem.getId(), updateDto));
        }

        @Test
        @DisplayName("ошибка при несуществующей вещи")
        void shouldThrowWhenItemNotFound() {
            ItemUpdateDto updateDto = makeItemUpdateDto("Хак", "Хак", true);

            assertThrows(NoSuchElementException.class,
                    () -> itemService.updateItem(owner.getId(), 999L, updateDto));
        }
    }

    @Nested
    @DisplayName("getItemById")
    class GetItemById {

        @Test
        @DisplayName("успешное получение вещи по id")
        void shouldReturnItem() {
            ItemDto created = itemService.createItem(owner.getId(),
                    makeItemDto("Дрель", "Простая дрель", true));

            ItemDto result = itemService.getItemById(created.getId());

            assertEquals(created.getId(), result.getId());
            assertEquals("Дрель", result.getName());
        }

        @Test
        @DisplayName("ошибка при несуществующем id")
        void shouldThrowWhenNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemService.getItemById(999L));
        }
    }

    @Nested
    @DisplayName("getItemsByOwnerId")
    class GetItemsByOwnerId {

        @Test
        @DisplayName("возвращает вещи владельца")
        void shouldReturnOwnerItems() {
            itemService.createItem(owner.getId(), makeItemDto("Дрель", "Описание", true));
            itemService.createItem(owner.getId(), makeItemDto("Пила", "Описание", true));
            itemService.createItem(otherUser.getId(), makeItemDto("Чужая вещь", "Описание", true));

            List<ItemDto> result = itemService.getItemsByOwnerId(owner.getId());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("пустой список если нет вещей")
        void shouldReturnEmptyList() {
            List<ItemDto> result = itemService.getItemsByOwnerId(owner.getId());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("ошибка при несуществующем пользователе")
        void shouldThrowWhenUserNotFound() {
            assertThrows(NoSuchElementException.class,
                    () -> itemService.getItemsByOwnerId(999L));
        }
    }

    @Nested
    @DisplayName("findAvailableItems")
    class FindAvailableItems {

        @BeforeEach
        void createItems() {
            itemService.createItem(owner.getId(), makeItemDto("Дрель ударная", "Мощная дрель", true));
            itemService.createItem(owner.getId(), makeItemDto("Пила", "Для дерева", true));
            itemService.createItem(owner.getId(), makeItemDto("Отвёртка", "Крестовая дрель-шуруповёрт", false));
        }

        @Test
        @DisplayName("поиск по названию")
        void shouldFindByName() {
            List<ItemDto> result = itemService.findAvailableItems("дрель");

            assertEquals(1, result.size());
            assertEquals("Дрель ударная", result.get(0).getName());
        }

        @Test
        @DisplayName("поиск по описанию")
        void shouldFindByDescription() {
            List<ItemDto> result = itemService.findAvailableItems("дерев");

            assertEquals(1, result.size());
            assertEquals("Пила", result.get(0).getName());
        }

        @Test
        @DisplayName("не находит недоступные вещи")
        void shouldNotFindUnavailable() {
            List<ItemDto> result = itemService.findAvailableItems("шуруповёрт");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("пустая строка возвращает пустой список")
        void shouldReturnEmptyForBlankText() {
            assertTrue(itemService.findAvailableItems("").isEmpty());
            assertTrue(itemService.findAvailableItems("   ").isEmpty());
        }

        @Test
        @DisplayName("null возвращает пустой список")
        void shouldReturnEmptyForNull() {
            assertTrue(itemService.findAvailableItems(null).isEmpty());
        }

        @Test
        @DisplayName("нет совпадений — пустой список")
        void shouldReturnEmptyWhenNoMatch() {
            assertTrue(itemService.findAvailableItems("самокат").isEmpty());
        }
    }
}