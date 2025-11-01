package com.chocolog.api.controller;

import com.chocolog.api.dto.request.PaymentPatchRequestDTO;
import com.chocolog.api.dto.request.PaymentRequestDTO;
import com.chocolog.api.dto.response.PaymentResponseDTO;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers/{customerId}/orders/{orderId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> addItemToOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequestDTO paymentDTO,
            Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getEmployeeId();
        PaymentResponseDTO createdItem = paymentService.addPayment(customerId, orderId, employeeId, paymentDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdItem);
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentPatchRequestDTO paymentDTO) {

        return ResponseEntity.ok(paymentService.updatePayment(customerId, orderId, paymentId, paymentDTO));
    }

    @DeleteMapping("/{paymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @PathVariable Long paymentId) {

        paymentService.deletePayment(customerId, orderId, paymentId);
    }
}