package com.ecommerce.project.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;
}
