package com.ecommerce.project.exceptions;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private String status;

    private String message;

    private Object data;


}
