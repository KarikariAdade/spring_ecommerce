package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long productId;

    @NotBlank
    @Size(min = 5, message = "Product name should be at least 5 characters")
    private String productName;

    @NotBlank
    @Size(min = 5, message = "Product name should be at least 5 characters")
    private String description;

    private String image;

    private Integer quantity;

    private double price;

    private double discount;

    private double specialPrice;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER) // product can belong to many cart items
    private List<CartItem> products = new ArrayList<>();

}
