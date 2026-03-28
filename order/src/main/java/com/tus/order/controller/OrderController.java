package com.tus.order.controller;

import com.tus.order.dto.OrderResponse;
import com.tus.order.service.OrderService;
import com.tus.order.dto.OrderRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // CREATE ORDER
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest req) {
        return ResponseEntity.ok(service.create(req));
    }


    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAll(
            @PageableDefault(page = 0, size = 5) Pageable pageable
    ) {
        Page<OrderResponse> page = service.getAll(pageable);

        return ResponseEntity.ok(new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        ));
    }

    //  GET ORDER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


     // DATE RANGE FILTER + PAGINATION

    @GetMapping("/range")
    public ResponseEntity<PagedResponse<OrderResponse>> getByDateRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(page = 0, size = 5) Pageable pageable
    ) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        Page<OrderResponse> page = service.getByDateRange(from, to, pageable);

        return ResponseEntity.ok(new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        ));
    }

    //  Response wrapper (shows required pagination info)
    public record PagedResponse<T>(
            List<T> content,
            int currentPage,
            int pageSize,
            long totalElements
    ) {}
}