package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    private Long addressId;

    private String paymentMethod;

    private String pgName;

    private String pgPaymentId;

    private String pgStatus;

    private String pgResponseMessage;

}
