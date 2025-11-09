package com.chocolog.api.controller;

import com.chocolog.api.service.StockRecordService;
import com.chocolog.api.dto.request.StockRecordRequestDTO;
import com.chocolog.api.dto.response.StockRecordResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/stock-records")
@RequiredArgsConstructor
@Tag(name = "Registros de Estoque", description = "API para gerenciamento de registros de estoque")
public class StockRecordController {

    private final StockRecordService stockRecordService;

    @Operation(summary = "Listar todos os registros de estoque", description = "Retorna uma lista com todos os registros de movimentação de estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de registros retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockRecordResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<StockRecordResponseDTO>> getAllStockRecords() {
        return ResponseEntity.ok(stockRecordService.findAll());
    }

    @Operation(summary = "Criar novo registro de estoque", description = "Registra uma nova movimentação de estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockRecordResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<StockRecordResponseDTO> createStockRecord(@Valid @RequestBody StockRecordRequestDTO requestDTO) {
        StockRecordResponseDTO createdStockRecord = stockRecordService.save(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStockRecord.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdStockRecord);
    }
}
