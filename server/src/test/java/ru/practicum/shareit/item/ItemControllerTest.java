package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_whenValid_returns201() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная", true, null);
        ItemDto saved = ItemDto.builder()
                .id(1L).name("Дрель").description("Мощная").available(true).build();

        when(itemService.createItem(eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_whenNoHeader_returns400() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenValid_returns200() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Дрель Pro", null, false);
        ItemDto updated = ItemDto.builder()
                .id(1L).name("Дрель Pro").description("Мощная").available(false).build();

        when(itemService.updateItem(eq(1L), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель Pro"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItem_whenExists_returns200() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L).name("Дрель").description("Мощная").available(true).build();

        when(itemService.getItemById(1L)).thenReturn(dto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItem_whenNotFound_returns404() throws Exception {
        when(itemService.getItemById(99L))
                .thenThrow(new NoSuchElementException("Вещь не найдена"));

        mockMvc.perform(get("/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Вещь не найдена"));
    }

    @Test
    void getUserItems_returns200() throws Exception {
        List<ItemDto> items = List.of(
                ItemDto.builder().id(1L).name("Дрель").available(true).build(),
                ItemDto.builder().id(2L).name("Пила").available(true).build()
        );

        when(itemService.getItemsByOwnerId(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].name").value("Пила"));
    }

    @Test
    void searchItems_whenValidText_returns200() throws Exception {
        List<ItemDto> items = List.of(
                ItemDto.builder().id(1L).name("Дрель").available(true).build()
        );

        when(itemService.findAvailableItems("дрель")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_whenBlankText_returnsEmpty() throws Exception {
        when(itemService.findAvailableItems("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createComment_whenValid_returns200() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");
        CommentDto saved = CommentDto.builder()
                .id(1L).text("Отличная дрель!").authorName("User")
                .created(LocalDateTime.now()).build();

        when(itemService.createComment(eq(1L), eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличная дрель!"))
                .andExpect(jsonPath("$.authorName").value("User"));
    }

    @Test
    void createComment_whenNoBooking_returns400() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        when(itemService.createComment(eq(1L), eq(1L), any()))
                .thenThrow(new IllegalArgumentException("Комментарий можно оставить только после завершённого бронирования"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Комментарий можно оставить только после завершённого бронирования"));
    }
}