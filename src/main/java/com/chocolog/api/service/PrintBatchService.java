package com.chocolog.api.service;

import com.chocolog.api.dto.request.PrintBatchRequestDTO;
import com.chocolog.api.dto.response.PrintBatchDetailResponseDTO;
import com.chocolog.api.dto.response.PrintBatchListResponseDTO;
import com.chocolog.api.mapper.PrintBatchMapper;
import com.chocolog.api.model.Employee;
import com.chocolog.api.model.OrderItem;
import com.chocolog.api.model.PrintBatch;
import com.chocolog.api.model.PrintBatchItem;
import com.chocolog.api.repository.EmployeeRepository;
import com.chocolog.api.repository.OrderItemRepository;
import com.chocolog.api.repository.PrintBatchItemRepository;
import com.chocolog.api.repository.PrintBatchRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrintBatchService {

    private final PrintBatchRepository printBatchRepository;
    private final PrintBatchItemRepository printBatchItemRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderItemRepository orderItemRepository;
    private final FileStorageService fileStorageService;
    private final PdfGenerationService pdfGenerationService;
    private final PrintBatchMapper printBatchMapper;

    public List<PrintBatchListResponseDTO> findAll() {
        return printBatchRepository.findAllWithEmployee().stream()
                .map(printBatchMapper::toListResponseDTO)
                .toList();
    }

    public PrintBatchDetailResponseDTO findById(Long id) {
        PrintBatch batch = printBatchRepository.findFullyLoadedBatchById(id)
                .orElseThrow(() -> new EntityNotFoundException("PrintBatch not found for id: " + id));
        return printBatchMapper.toDetailResponseDTO(batch);
    }

    @Transactional
    public PrintBatchDetailResponseDTO save(PrintBatchRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + requestDTO.getEmployeeId()));

        PrintBatch batch = PrintBatch.builder()
                .printedBy(employee)
                .createdAt(LocalDateTime.now())
                .build();
        PrintBatch savedBatch = printBatchRepository.save(batch);

        List<OrderItem> orderItems = orderItemRepository.findAllById(requestDTO.getOrderItemIds());
        List<PrintBatchItem> batchItems = new ArrayList<>();

        for (OrderItem item : orderItems) {
            batchItems.add(PrintBatchItem.builder()
                    .printBatch(savedBatch)
                    .orderItem(item)
                    .build());
        }

        List<PrintBatchItem> savedItems = printBatchItemRepository.saveAllAndFlush(batchItems);
        savedBatch.setItems(savedItems);

        try {
            generateAndStorePdf(savedBatch.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to process batch", e);
        }

        return findById(savedBatch.getId());
    }

    public void generateAndStorePdf(Long batchId) {
        try {
            PrintBatch batch = printBatchRepository.findFullyLoadedBatchById(batchId)
                    .orElseThrow(() -> new EntityNotFoundException("PrintBatch not found for id: " + batchId));

            byte[] pdfData = pdfGenerationService.generateBatchPdf(batch);
            String filePath = fileStorageService.save(pdfData, batchId);

            batch.setFileSystemPath(filePath);
            printBatchRepository.save(batch);

        } catch (Exception e) {
            System.err.println("Failed to generate and save PDF for batch id: " + batchId);
            e.printStackTrace();
        }
    }

    public Map.Entry<String, Resource> download(Long id) {
        PrintBatch batch = printBatchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PrintBatch not found for id: " + id));

        String filePath = batch.getFileSystemPath();
        if (filePath == null || filePath.isEmpty()) {
            throw new EntityNotFoundException("PDF file is not ready yet or failed.\n");
        }

        Resource resource = fileStorageService.loadAsResource(filePath);
        String filename = fileStorageService.getFilename(filePath);

        return new AbstractMap.SimpleEntry<>(filename, resource);
    }
}
