package com.chocolog.api.controller;

import com.chocolog.api.dto.request.OrderPatchRequestDTO;
import com.chocolog.api.dto.request.OrderRequestDTO;
import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers/{customerId}/orders")
@Tag(name = "Pedidos do Cliente", description = "API para gerenciamento de pedidos de um cliente específico")
public class CustomerOrderController {

    private final OrderService orderService;

    @Operation(summary = "Listar todos os pedidos de um cliente", description = "Retorna uma lista com todos os pedidos de um cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByCustomer(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.findAllByCustomerId(customerId));
    }

    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico de um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido ou cliente não encontrado", content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findByIdAndCustomerId(orderId, customerId));
    }

    @Operation(summary = "Criar novo pedido", description = "Cria um novo pedido para um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Valid @RequestBody OrderRequestDTO orderDTO,
            Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getEmployeeId();
        OrderResponseDTO createdOrder = orderService.create(customerId, orderDTO, employeeId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    @Operation(summary = "Atualizar parcialmente um pedido", description = "Atualiza campos específicos de um pedido existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido ou cliente não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> partialUpdateOrder(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Valid @RequestBody OrderPatchRequestDTO orderDTO) {
        return ResponseEntity.ok(orderService.update(orderId, customerId, orderDTO));
    }
}
