package com.chocolog.api.controller;

import com.chocolog.api.dto.request.OrderItemPatchRequestDTO;
import com.chocolog.api.dto.request.OrderItemRequestDTO;
import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.service.OrderItemService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers/{customerId}/orders/{orderId}/items")
@Tag(name = "Itens do Pedido", description = "API para gerenciamento de itens de um pedido")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "Adicionar item ao pedido", description = "Adiciona um novo item a um pedido existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou pedido não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<OrderItemResponseDTO> addItemToOrder(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequestDTO itemDTO) {

        OrderItemResponseDTO createdItem = orderItemService.addItemToOrder(customerId, orderId, itemDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdItem.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdItem);
    }

    @Operation(summary = "Atualizar item do pedido", description = "Atualiza um item específico de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente, pedido ou item não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PatchMapping("/{itemId}")
    public ResponseEntity<OrderItemResponseDTO> updateOrderItem(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Parameter(description = "ID do item") @PathVariable Long itemId,
            @Valid @RequestBody OrderItemPatchRequestDTO itemDTO) {

        return ResponseEntity.ok(orderItemService.updateOrderItem(customerId, orderId, itemId, itemDTO));
    }

    @Operation(summary = "Deletar item do pedido", description = "Remove um item de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente, pedido ou item não encontrado", content = @Content)
    })
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderItem(
            @Parameter(description = "ID do cliente") @PathVariable Long customerId,
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Parameter(description = "ID do item") @PathVariable Long itemId) {

        orderItemService.deleteOrderItem(customerId, orderId, itemId);
    }
}
