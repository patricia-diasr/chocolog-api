package com.chocolog.api.controller;

import com.chocolog.api.dto.request.OrderPatchRequestDTO;
import com.chocolog.api.dto.request.OrderRequestDTO;
import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers/{customerId}/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.findAllByCustomerId(customerId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long customerId, @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findByIdAndCustomerId(orderId, customerId));
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@PathVariable Long customerId, @Valid @RequestBody OrderRequestDTO orderDTO) {
        OrderResponseDTO createdOrder = orderService.create(customerId, orderDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> partialUpdateOrder(@PathVariable Long customerId, @PathVariable Long orderId, @Valid @RequestBody OrderPatchRequestDTO orderDTO) {
        return ResponseEntity.ok(orderService.update(orderId, customerId, orderDTO));
    }
}