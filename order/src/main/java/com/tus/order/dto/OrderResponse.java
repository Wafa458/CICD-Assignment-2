package com.tus.order.dto;

import java.time.LocalDate;

public class OrderResponse {

    private Long id;
    private String product;
    private Integer quantity;
    private Double price;
    private LocalDate orderDate;
    private Long customerId;
    private String customerName;

    public OrderResponse() {}

    public OrderResponse(Long id, String product, Integer quantity, Double price,
                         LocalDate orderDate, Long customerId, String customerName) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}

