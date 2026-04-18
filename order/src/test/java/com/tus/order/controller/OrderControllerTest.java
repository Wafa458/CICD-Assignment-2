package com.tus.order.controller;

import com.tus.order.dto.OrderRequest;
import com.tus.order.dto.OrderResponse;
import com.tus.order.exception.GlobalExceptionHandler;
import com.tus.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.time.LocalDate;
import java.util.List;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_shouldReturnOrder_whenValidRequest() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                "Laptop",
                2,
                999.99,
                LocalDate.of(2026, 3, 10),
                1L,
                "John Doe"
        );

        when(orderService.create(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "product": "Laptop",
                                  "quantity": 2,
                                  "price": 999.99,
                                  "orderDate": "2026-03-10"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product").value("Laptop"))
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    void create_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.customerId").value("customerId is required"))
                .andExpect(jsonPath("$.fieldErrors.product").value("product is required"));
    }

    @Test
    void getAll_shouldReturnPagedResponse() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        OrderResponse order = new OrderResponse(1L, "Laptop", 2, 999.99, LocalDate.of(2026, 3, 10), 1L, "John Doe");

        when(orderService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        mockMvc.perform(get("/api/orders?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturnOrder_whenFound() throws Exception {
        OrderResponse order = new OrderResponse(1L, "Laptop", 2, 999.99, LocalDate.of(2026, 3, 10), 1L, "John Doe");
        when(orderService.getById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product").value("Laptop"));
    }

    @Test
    void getById_shouldReturnNotFound_whenMissing() throws Exception {
        when(orderService.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDateRange_shouldReturnPagedResponse() throws Exception {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        Pageable pageable = PageRequest.of(0, 5);

        OrderResponse order = new OrderResponse(1L, "Laptop", 2, 999.99, LocalDate.of(2026, 3, 10), 1L, "John Doe");
        when(orderService.getByDateRange(eq(from), eq(to), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        mockMvc.perform(get("/api/orders/range?from=2026-03-01&to=2026-03-31&page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getByDateRange_shouldReturnBadRequest_whenFromAfterTo() throws Exception {
        mockMvc.perform(get("/api/orders/range?from=2026-03-31&to=2026-03-01"))
                .andExpect(status().isBadRequest());
    }
}
