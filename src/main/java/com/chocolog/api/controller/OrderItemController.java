package com.chocolog.api.controller;

import com.chocolog.api.dto.request.OrderItemPatchRequestDTO;
import com.chocolog.api.dto.request.OrderItemRequestDTO;
import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers/{customerId}/orders/{orderId}/items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponseDTO> addItemToOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequestDTO itemDTO) {

        OrderItemResponseDTO createdItem = orderItemService.addItemToOrder(customerId, orderId, itemDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<OrderItemResponseDTO> updateOrderItem(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody OrderItemPatchRequestDTO itemDTO) {

        return ResponseEntity.ok(orderItemService.updateOrderItem(customerId, orderId, itemId, itemDTO));
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderItem(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {

        orderItemService.deleteOrderItem(customerId, orderId, itemId);
    }
}