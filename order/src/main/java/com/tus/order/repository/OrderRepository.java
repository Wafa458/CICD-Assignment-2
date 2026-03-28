package com.tus.order.repository;

import com.tus.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //  date-range filtering + pagination
    Page<Order> findByOrderDateBetween(LocalDate from, LocalDate to, Pageable pageable);
}