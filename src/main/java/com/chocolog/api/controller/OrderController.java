package com.chocolog.api.controller;

import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDate(
            @RequestParam(name = "date") String dateString
    ) {
        List<OrderResponseDTO> orders = orderService.findAllByDateFilter(dateString);
        return ResponseEntity.ok(orders);
    }

}