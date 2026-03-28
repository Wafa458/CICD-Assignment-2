package com.tus.order.service;

import com.tus.order.model.Customer;
import com.tus.order.repository.CustomerRepository;
import com.tus.order.model.Order;
import com.tus.order.repository.OrderRepository;
import com.tus.order.dto.OrderRequest;
import com.tus.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;

    public OrderService(OrderRepository orderRepo, CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
    }

    // Convert Entity -> DTO
    private OrderResponse toDTO(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getProduct(),
                o.getQuantity(),
                o.getPrice(),
                o.getOrderDate(),
                o.getCustomer().getId(),
                o.getCustomer().getName()
        );
    }

    public OrderResponse create(OrderRequest req) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + req.getCustomerId()));

        Order order = new Order();
        order.setProduct(req.getProduct());
        order.setQuantity(req.getQuantity());
        order.setPrice(req.getPrice());
        order.setOrderDate(req.getOrderDate());
        order.setCustomer(customer);

        Order saved = orderRepo.save(order);
        return toDTO(saved);
    }

    // Pagination for all orders
    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepo.findAll(pageable).map(this::toDTO);
    }

    public Optional<OrderResponse> getById(Long id) {
        return orderRepo.findById(id).map(this::toDTO);
    }

    //  Date range + pagination
    public Page<OrderResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        return orderRepo.findByOrderDateBetween(from, to, pageable).map(this::toDTO);
    }
}