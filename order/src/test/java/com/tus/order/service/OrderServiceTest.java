package com.tus.order.service;

import com.tus.order.dto.OrderRequest;
import com.tus.order.dto.OrderResponse;
import com.tus.order.model.Customer;
import com.tus.order.model.Order;
import com.tus.order.repository.CustomerRepository;
import com.tus.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer("John Doe", "john@example.com");
        customer.setId(1L);
        customer.setOrders(new ArrayList<>());

        order = new Order();
        order.setProduct("Laptop");
        order.setQuantity(2);
        order.setPrice(999.99);
        order.setOrderDate(LocalDate.of(2026, 3, 10));
        order.setCustomer(customer);

        // Use reflection to set the id since there's no setter
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        orderRequest = new OrderRequest();
        orderRequest.setCustomerId(1L);
        orderRequest.setProduct("Laptop");
        orderRequest.setQuantity(2);
        orderRequest.setPrice(999.99);
        orderRequest.setOrderDate(LocalDate.of(2026, 3, 10));
    }

    // ==================== create ====================

    @Test
    @DisplayName("create - should create order and return OrderResponse")
    void create_shouldCreateAndReturn() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.create(orderRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getProduct());
        assertEquals(2, result.getQuantity());
        assertEquals(999.99, result.getPrice());
        assertEquals(LocalDate.of(2026, 3, 10), result.getOrderDate());
        assertEquals(1L, result.getCustomerId());
        assertEquals("John Doe", result.getCustomerName());

        verify(customerRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("create - should throw RuntimeException when customer not found")
    void create_shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        orderRequest.setCustomerId(99L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.create(orderRequest));

        assertTrue(ex.getMessage().contains("Customer not found"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should map all request fields to entity")
    void create_shouldMapAllFields() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            try {
                var idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(savedOrder, 10L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return savedOrder;
        });

        OrderResponse result = orderService.create(orderRequest);

        assertEquals("Laptop", result.getProduct());
        assertEquals(2, result.getQuantity());
        assertEquals(999.99, result.getPrice());
        assertEquals(LocalDate.of(2026, 3, 10), result.getOrderDate());
    }

    // ==================== getAll ====================

    @Test
    @DisplayName("getAll - should return paginated OrderResponse")
    void getAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getProduct());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAll - should return empty page when no orders")
    void getAll_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<OrderResponse> result = orderService.getAll(pageable);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("getAll - should correctly map DTO fields in page")
    void getAll_shouldMapDTOFields() {
        Order order2 = new Order();
        order2.setProduct("Phone");
        order2.setQuantity(1);
        order2.setPrice(599.99);
        order2.setOrderDate(LocalDate.of(2026, 3, 11));
        order2.setCustomer(customer);
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order2, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> orderPage = new PageImpl<>(List.of(order, order2), pageable, 2);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getProduct());
        assertEquals("Phone", result.getContent().get(1).getProduct());
        assertEquals(1L, result.getContent().get(0).getCustomerId());
    }

    // ==================== getById ====================

    @Test
    @DisplayName("getById - should return OrderResponse when found")
    void getById_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<OrderResponse> result = orderService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Laptop", result.get().getProduct());
        assertEquals(2, result.get().getQuantity());
        assertEquals(999.99, result.get().getPrice());
        assertEquals(1L, result.get().getCustomerId());
        assertEquals("John Doe", result.get().getCustomerName());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getById - should return empty when not found")
    void getById_shouldReturnEmpty() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<OrderResponse> result = orderService.getById(99L);

        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findById(99L);
    }

    // ==================== getByDateRange ====================

    @Test
    @DisplayName("getByDateRange - should return filtered orders by date range")
    void getByDateRange_shouldReturnFilteredOrders() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        Pageable pageable = PageRequest.of(0, 5);

        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByOrderDateBetween(from, to, pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getByDateRange(from, to, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getProduct());
        verify(orderRepository, times(1)).findByOrderDateBetween(from, to, pageable);
    }

    @Test
    @DisplayName("getByDateRange - should return empty page when no orders in range")
    void getByDateRange_shouldReturnEmptyWhenNoOrders() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        Pageable pageable = PageRequest.of(0, 5);

        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findByOrderDateBetween(from, to, pageable)).thenReturn(emptyPage);

        Page<OrderResponse> result = orderService.getByDateRange(from, to, pageable);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("getByDateRange - should handle pagination correctly")
    void getByDateRange_shouldHandlePagination() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        Pageable pageable = PageRequest.of(1, 2); // second page, 2 items per page

        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 5); // total 5 elements

        when(orderRepository.findByOrderDateBetween(from, to, pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getByDateRange(from, to, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(1, result.getNumber()); // page 1 (0-indexed)
    }
}

