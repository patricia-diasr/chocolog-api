package com.chocolog.api.controller;

import com.chocolog.api.dto.request.PrintBatchRequestDTO;
import com.chocolog.api.dto.response.*;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.OrderItemService;
import com.chocolog.api.service.OrderService;
import com.chocolog.api.service.PrintBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "API para gerenciamento geral de pedidos e lotes de impressão")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PrintBatchService printBatchService;

    @Operation(summary = "Buscar pedidos por data", description = "Retorna todos os pedidos filtrados por data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDate(
            @Parameter(description = "Data no formato yyyy-MM-dd") @RequestParam(name = "date") String dateString) {
        List<OrderResponseDTO> orders = orderService.findAllByDateFilter(dateString);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Listar itens de pedidos", description = "Retorna todos os itens de pedidos, opcionalmente filtrados por sob demanda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderItemResponseDTO.class)))
    })
    @GetMapping("/items")
    public ResponseEntity<List<OrderItemResponseDTO>> getOrdersItems(
            @Parameter(description = "Filtrar apenas itens sob demanda") @RequestParam(name = "onDemand", defaultValue = "false") Boolean onDemand) {
        List<OrderItemResponseDTO> items = orderItemService.findAll(onDemand);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Listar lotes de impressão", description = "Retorna todos os lotes de impressão cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de lotes retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrintBatchListResponseDTO.class)))
    })
    @GetMapping("/print-batchs")
    public ResponseEntity<List<PrintBatchListResponseDTO>> getPrintBactchs() {
        List<PrintBatchListResponseDTO> printBatchs = printBatchService.findAll();
        return ResponseEntity.ok(printBatchs);
    }

    @Operation(summary = "Buscar lote de impressão por ID", description = "Retorna os detalhes de um lote de impressão específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrintBatchDetailResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content)
    })
    @GetMapping("/print-batchs/{id}")
    public ResponseEntity<PrintBatchDetailResponseDTO> getPrintBactchsById(
            @Parameter(description = "ID do lote de impressão") @PathVariable("id") Long id) {
        PrintBatchDetailResponseDTO printBatchs = printBatchService.findById(id);
        return ResponseEntity.ok(printBatchs);
    }

    @Operation(summary = "Criar lote de impressão", description = "Cria um novo lote de impressão com os pedidos selecionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrintBatchDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/print-batchs")
    public ResponseEntity<PrintBatchDetailResponseDTO> createPrintBatch(
            @Valid @RequestBody PrintBatchRequestDTO printBatchDTO,
            Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getEmployeeId();

        PrintBatchDetailResponseDTO createdPrint = printBatchService.save(printBatchDTO, employeeId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPrint.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdPrint);
    }

    @Operation(summary = "Download do lote de impressão", description = "Faz o download do PDF de um lote de impressão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content)
    })
    @GetMapping("/print-batchs/{id}/download")
    public ResponseEntity<Resource> downloadPrintBatch(
            @Parameter(description = "ID do lote de impressão") @PathVariable("id") Long id) {
        Map.Entry<String, Resource> fileData = printBatchService.download(id);
        String filename = fileData.getKey();
        Resource resource = fileData.getValue();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
