package com.chocolog.api.controller;

import com.chocolog.api.service.StockRecordService;
import com.chocolog.api.dto.request.StockRecordRequestDTO;
import com.chocolog.api.dto.response.StockRecordResponseDTO;

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
public class StockRecordController {

    private final StockRecordService stockRecordService;

    @GetMapping
    public ResponseEntity<List<StockRecordResponseDTO>> getAllStockRecords() {
        return ResponseEntity.ok(stockRecordService.findAll());
    }

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