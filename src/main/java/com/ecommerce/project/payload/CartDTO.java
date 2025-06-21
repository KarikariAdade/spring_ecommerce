package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long cartId;

    private Double totalPrice = 0.0;

    private List<ProductDTO> products = new ArrayList<>();

}
