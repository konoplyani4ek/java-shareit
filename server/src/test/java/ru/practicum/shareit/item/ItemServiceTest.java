package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemRequestRepository itemRequestRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private ItemService itemService;

    private final User owner = new User(1L, "Owner", "owner@test.com");

    @Test
    void createItem_whenValid_returnsDto() {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная", true, null);
        Item saved = Item.builder()
                .id(1L).name("Дрель").description("Мощная")
                .available(true).owner(owner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(saved);

        ItemDto result = itemService.createItem(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void createItem_whenUserNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> itemService.createItem(99L, new ItemCreateDto()));
    }

    @Test
    void updateItem_whenNotOwner_throwsForbidden() {
        User other = new User(2L, "Other", "other@test.com");
        Item item = Item.builder().id(1L).owner(owner).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(other));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(2L, 1L, new ItemUpdateDto()));
    }

    @Test
    void updateItem_whenOwner_updatesFields() {
        Item item = Item.builder()
                .id(1L).name("Старое").description("Старое описание")
                .available(true).owner(owner).build();
        ItemUpdateDto dto = new ItemUpdateDto("Новое", "Новое описание", false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, 1L, dto);

        assertEquals("Новое", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void getItemById_whenNotFound_throwsException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> itemService.getItemById(99L));
    }

    @Test
    void findAvailableItems_whenBlankText_returnsEmpty() {
        List<ItemDto> result = itemService.findAvailableItems("  ");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchByText(any());
    }

    @Test
    void findAvailableItems_whenValidText_returnsItems() {
        Item item = Item.builder()
                .id(1L).name("Дрель").description("Мощная")
                .available(true).owner(owner).build();

        when(itemRepository.searchByText("дрель")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.findAvailableItems("дрель");

        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void createComment_whenNoBooking_throwsException() {
        User user = new User(2L, "User", "user@test.com");
        Item item = Item.builder().id(1L).owner(owner).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateTimeBefore(
                anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.createComment(2L, 1L, new CommentCreateDto("Отлично!")));
    }

    @Test
    void createComment_whenValid_returnsDto() {
        User user = new User(2L, "User", "user@test.com");
        Item item = Item.builder().id(1L).owner(owner).build();
        Comment saved = Comment.builder()
                .id(1L).text("Отлично!").item(item).author(user)
                .created(LocalDateTime.now()).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateTimeBefore(
                anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any())).thenReturn(saved);

        CommentDto result = itemService.createComment(2L, 1L, new CommentCreateDto("Отлично!"));

        assertEquals("Отлично!", result.getText());
        assertEquals("User", result.getAuthorName());
    }
}