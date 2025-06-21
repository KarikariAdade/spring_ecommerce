package com.ecommerce.project.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class ResourceNotFoundException extends RuntimeException{

    String resourceName;

    String field;

    String fieldName;

    Long fieldId;

    public ResourceNotFoundException () {

    }

    public ResourceNotFoundException(String resourceName, String field, String fieldName) {

        super(String.format("Resource %s not found for field %s: %s", resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;

    }


}
