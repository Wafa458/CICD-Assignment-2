package com.tus.order.controller;

import com.tus.order.dto.CustomerRequest;
import com.tus.order.dto.CustomerResponse;
import com.tus.order.exception.GlobalExceptionHandler;
import com.tus.order.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAll_shouldReturnCustomers() throws Exception {
        when(customerService.getAll()).thenReturn(List.of(
                new CustomerResponse(1L, "John Doe", "john@example.com", 2),
                new CustomerResponse(2L, "Jane Doe", "jane@example.com", 0)
        ));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].totalOrders").value(2))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getById_shouldReturnCustomer_whenFound() throws Exception {
        when(customerService.getById(1L))
                .thenReturn(Optional.of(new CustomerResponse(1L, "John Doe", "john@example.com", 1)));

        mockMvc.perform(get("/api/customers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getById_shouldReturnNotFound_whenMissing() throws Exception {
        when(customerService.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnCustomer_whenValidRequest() throws Exception {
        CustomerResponse response = new CustomerResponse(1L, "John Doe", "john@example.com", 0);
        when(customerService.create(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Doe","email":"john@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void create_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void update_shouldReturnCustomer_whenFound() throws Exception {
        CustomerResponse response = new CustomerResponse(1L, "John Updated", "updated@example.com", 3);
        when(customerService.update(eq(1L), any(CustomerRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Updated","email":"updated@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.totalOrders").value(3));
    }

    @Test
    void update_shouldReturnNotFound_whenMissing() throws Exception {
        when(customerService.update(eq(999L), any(CustomerRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/customers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Updated","email":"updated@example.com"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnNoContent_whenDeleted() throws Exception {
        when(customerService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }

    @Test
    void delete_shouldReturnNotFound_whenMissing() throws Exception {
        when(customerService.delete(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(customerService).delete(999L);
    }
}
