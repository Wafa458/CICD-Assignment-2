package com.tus.order.service;

import com.tus.order.dto.CustomerRequest;
import com.tus.order.dto.CustomerResponse;
import com.tus.order.model.Customer;
import com.tus.order.model.Order;
import com.tus.order.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer("John Doe", "john@example.com");
        customer.setId(1L);
        customer.setOrders(new ArrayList<>());

        customerRequest = new CustomerRequest("John Doe", "john@example.com");
    }

    // ==================== getAll ====================

    @Test
    @DisplayName("getAll - should return list of CustomerResponse")
    void getAll_shouldReturnList() {
        Customer customer2 = new Customer("Jane Doe", "jane@example.com");
        customer2.setId(2L);
        customer2.setOrders(new ArrayList<>());

        when(customerRepository.findAll()).thenReturn(List.of(customer, customer2));

        List<CustomerResponse> result = customerService.getAll();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("jane@example.com", result.get(1).getEmail());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAll - should return empty list when no customers")
    void getAll_shouldReturnEmptyList() {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        List<CustomerResponse> result = customerService.getAll();

        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAll - should correctly map totalOrders")
    void getAll_shouldMapTotalOrders() {
        Order order = new Order();
        order.setCustomer(customer);
        customer.getOrders().add(order);

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> result = customerService.getAll();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTotalOrders());
    }

    // ==================== getById ====================

    @Test
    @DisplayName("getById - should return CustomerResponse when found")
    void getById_shouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<CustomerResponse> result = customerService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("John Doe", result.get().getName());
        assertEquals("john@example.com", result.get().getEmail());
        assertEquals(0, result.get().getTotalOrders());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getById - should return empty Optional when not found")
    void getById_shouldReturnEmpty() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CustomerResponse> result = customerService.getById(99L);

        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findById(99L);
    }

    // ==================== create ====================

    @Test
    @DisplayName("create - should save and return CustomerResponse")
    void create_shouldSaveAndReturn() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse result = customerService.create(customerRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(0, result.getTotalOrders());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("create - should map request fields to entity correctly")
    void create_shouldMapRequestToEntity() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CustomerResponse result = customerService.create(new CustomerRequest("Alice", "alice@test.com"));

        assertEquals("Alice", result.getName());
        assertEquals("alice@test.com", result.getEmail());
    }

    // ==================== update ====================

    @Test
    @DisplayName("update - should update existing customer and return response")
    void update_shouldUpdateAndReturn() {
        CustomerRequest updateReq = new CustomerRequest("Updated Name", "updated@example.com");

        Customer updatedCustomer = new Customer("Updated Name", "updated@example.com");
        updatedCustomer.setId(1L);
        updatedCustomer.setOrders(new ArrayList<>());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        Optional<CustomerResponse> result = customerService.update(1L, updateReq);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("updated@example.com", result.get().getEmail());
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("update - should return empty when customer not found")
    void update_shouldReturnEmptyWhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CustomerResponse> result = customerService.update(99L, customerRequest);

        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findById(99L);
        verify(customerRepository, never()).save(any());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete - should return true when customer exists")
    void delete_shouldReturnTrue() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(customer);

        boolean result = customerService.delete(1L);

        assertTrue(result);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("delete - should return false when customer not found")
    void delete_shouldReturnFalse() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = customerService.delete(99L);

        assertFalse(result);
        verify(customerRepository, times(1)).findById(99L);
        verify(customerRepository, never()).delete(any());
    }

    // ==================== DTO mapping edge cases ====================

    @Test
    @DisplayName("toDTO - should handle null orders list gracefully")
    void toDTO_shouldHandleNullOrders() {
        customer.setOrders(null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<CustomerResponse> result = customerService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTotalOrders());
    }
}

