package com.ecommerce.project.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;

    private String productName;

    private String image;

    private Integer quantity;

    private String description;

    private double price;

    private double discount;

    private double specialPrice;




}
