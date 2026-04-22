package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .status(Status.WAITING)
            .item(BookingDto.ItemInfo.builder().id(1L).name("Дрель").build())
            .booker(BookingDto.UserInfo.builder().id(2L).build())
            .build();

    @Test
    void createBooking_whenValid_returns201() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingService.createBooking(eq(2L), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_whenOwner_returns200() throws Exception {
        BookingDto approved = BookingDto.builder()
                .id(1L).status(Status.APPROVED)
                .item(BookingDto.ItemInfo.builder().id(1L).name("Дрель").build())
                .booker(BookingDto.UserInfo.builder().id(2L).build())
                .build();

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true))).thenReturn(approved);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingInfo_whenValid_returns200() throws Exception {
        when(bookingService.getBookingInfo(eq(1L), eq(1L))).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingInfo_whenNotFound_returns404() throws Exception {
        when(bookingService.getBookingInfo(eq(1L), eq(99L)))
                .thenThrow(new NoSuchElementException("Букинг не найден"));

        mockMvc.perform(get("/bookings/99")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Букинг не найден"));
    }

    @Test
    void getUserBookings_stateAll_returns200() throws Exception {
        when(bookingService.getUserBookings(eq(2L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserBookings_defaultState_returns200() throws Exception {
        when(bookingService.getUserBookings(eq(2L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_stateAll_returns200() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOwnerBookings_whenUserNotFound_returns404() throws Exception {
        when(bookingService.getOwnerBookings(eq(99L), eq(BookingState.ALL)))
                .thenThrow(new NoSuchElementException("Пользователь не найден"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 99L)
                        .param("state", "ALL"))
                .andExpect(status().isNotFound());
    }
}