package com.ecommerce.project.payload;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long addressId;

    private String street;

    private String buildingName;

    private String city;

    private String state;

    private String country;

    private String pincode;
}
