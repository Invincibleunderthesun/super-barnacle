package com.harsh.firstApp.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One cart can have many products
    @ManyToMany
    private List<Product> products = new ArrayList<>();

    public Cart() {}

    public Cart(List<Product> products) {
        this.products = products;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
