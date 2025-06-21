package com.ecommerce.project.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SignUpRequest {

    @NotBlank
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private Set<String> role;

}
