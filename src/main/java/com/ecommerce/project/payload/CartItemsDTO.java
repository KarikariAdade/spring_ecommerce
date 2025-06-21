package com.ecommerce.project.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsDTO {

    private Long cartItemId;

    private CartDTO cart;

    private ProductDTO product;

    private Integer quantity;

    private Double discount;

    private double productPrice;

}
