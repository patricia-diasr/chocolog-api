package com.chocolog.api.controller;

import com.chocolog.api.dto.request.PaymentPatchRequestDTO;
import com.chocolog.api.dto.request.PaymentRequestDTO;
import com.chocolog.api.dto.response.PaymentResponseDTO;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pagamentos", description = "API para gerenciamento de pagamentos de pedidos")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Adicionar pagamento ao pedido", description = "Registra um novo pagamento para um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou pedido não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> addItemToOrder(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
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

    @Operation(summary = "Atualizar pagamento", description = "Atualiza as informações de um pagamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente, pedido ou pagamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Parameter(description = "ID do pagamento") @PathVariable Long paymentId,
            @Valid @RequestBody PaymentPatchRequestDTO paymentDTO) {

        return ResponseEntity.ok(paymentService.updatePayment(customerId, orderId, paymentId, paymentDTO));
    }

    @Operation(summary = "Deletar pagamento", description = "Remove um pagamento de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pagamento deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente, pedido ou pagamento não encontrado", content = @Content)
    })
    @DeleteMapping("/{paymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Parameter(description = "ID do pagamento") @PathVariable Long paymentId) {

        paymentService.deletePayment(customerId, orderId, paymentId);
    }
}
