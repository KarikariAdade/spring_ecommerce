package com.ecommerce.project.payload;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long orderItemId;

    private ProductDTO product;

    private Integer quantity;

    private double discount;

    private double orderedProductPrice;


}
