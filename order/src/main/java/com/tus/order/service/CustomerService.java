package com.tus.order.service;

import com.tus.order.model.Customer;
import com.tus.order.repository.CustomerRepository;
import com.tus.order.dto.CustomerRequest;
import com.tus.order.dto.CustomerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    // Convert Entity -> DTO
    private CustomerResponse toDTO(Customer c) {
        int totalOrders = (c.getOrders() == null) ? 0 : c.getOrders().size();
        return new CustomerResponse(
                c.getId(),
                c.getName(),
                c.getEmail(),
                totalOrders
        );
    }

    // GET ALL
    public List<CustomerResponse> getAll() {
        return repo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // GET BY ID
    public Optional<CustomerResponse> getById(Long id) {
        return repo.findById(id)
                .map(this::toDTO);
    }

    // CREATE
    @Transactional
    public CustomerResponse create(CustomerRequest req) {
        Customer customer = new Customer();
        customer.setName(req.getName());
        customer.setEmail(req.getEmail());
        Customer saved = repo.save(customer);
        return toDTO(saved);
    }

    // UPDATE
    @Transactional
    public Optional<CustomerResponse> update(Long id, CustomerRequest req) {
        return repo.findById(id).map(existing -> {

            existing.setName(req.getName());
            existing.setEmail(req.getEmail());

            Customer saved = repo.save(existing);
            return toDTO(saved);
        });
    }

    // DELETE
    @Transactional
    public boolean delete(Long id) {
        return repo.findById(id).map(customer -> {
            repo.delete(customer);
            return true;
        }).orElse(false);
    }
}