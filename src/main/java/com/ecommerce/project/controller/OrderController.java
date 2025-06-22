package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    @PostMapping("")
    public ResponseEntity<OrderDTO> orderProducts() {



    }


}
