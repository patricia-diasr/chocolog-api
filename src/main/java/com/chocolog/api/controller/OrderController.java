package com.chocolog.api.controller;

import com.chocolog.api.dto.request.PrintBatchRequestDTO;
import com.chocolog.api.dto.response.*;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.OrderItemService;
import com.chocolog.api.service.OrderService;
import com.chocolog.api.service.PrintBatchService;
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
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PrintBatchService printBatchService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDate(@RequestParam(name = "date") String dateString) {
        List<OrderResponseDTO> orders = orderService.findAllByDateFilter(dateString);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/items")
    public ResponseEntity<List<OrderItemResponseDTO>> getOrdersItems(@RequestParam(name = "onDemand", defaultValue = "false") Boolean onDemand) {
        List<OrderItemResponseDTO> items = orderItemService.findAll(onDemand);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/print-batchs")
    public ResponseEntity<List<PrintBatchListResponseDTO>> getPrintBactchs() {
        List<PrintBatchListResponseDTO> printBatchs = printBatchService.findAll();
        return ResponseEntity.ok(printBatchs);
    }

    @GetMapping("/print-batchs/{id}")
    public ResponseEntity<PrintBatchDetailResponseDTO> getPrintBactchsById(@PathVariable("id") Long id) {
        PrintBatchDetailResponseDTO printBatchs = printBatchService.findById(id);
        return ResponseEntity.ok(printBatchs);
    }

    @PostMapping("/print-batchs")
    public ResponseEntity<PrintBatchDetailResponseDTO> createPrintBatch(@Valid @RequestBody PrintBatchRequestDTO printBatchDTO, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getEmployeeId();

        PrintBatchDetailResponseDTO createdPrint = printBatchService.save(printBatchDTO, employeeId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPrint.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdPrint);
    }

    @GetMapping("/print-batchs/{id}/download")
    public ResponseEntity<Resource> downloadPrintBatch(@PathVariable("id") Long id) {
        Map.Entry<String, Resource> fileData = printBatchService.download(id);
        String filename = fileData.getKey();
        Resource resource = fileData.getValue();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}