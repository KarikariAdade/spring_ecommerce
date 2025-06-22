package com.ecommerce.project.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long paymentId;

    private String paymentMethod;

    private String pgPaymentId;

    private String pgStatus;

    private String pgResponseMessage;

    private String pgName;


}
