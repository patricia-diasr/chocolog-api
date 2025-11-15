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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para PrintBatchService")
public class PrintBatchServiceTest {

    @Mock
    private PrintBatchRepository printBatchRepository;
    @Mock
    private PrintBatchItemRepository printBatchItemRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private PdfGenerationService pdfGenerationService;
    @Mock
    private PrintBatchMapper printBatchMapper;

    @InjectMocks
    private PrintBatchService printBatchService;

    private Employee mockEmployee;
    private OrderItem mockOrderItem1;
    private OrderItem mockOrderItem2;
    private PrintBatch mockBatch;
    private PrintBatchRequestDTO mockRequestDTO;
    private PrintBatchListResponseDTO mockListResponseDTO;
    private PrintBatchDetailResponseDTO mockDetailResponseDTO;
    private Resource mockResource;

    private final Long employeeId = 1L;
    private final Long batchId = 1L;
    private final Long orderItemId1 = 10L;
    private final Long orderItemId2 = 11L;
    private final String mockFilePath = "path/to/generated-file.pdf";
    private final String mockFilename = "generated-file.pdf";

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .id(employeeId)
                .name("Funcionário Impressão")
                .build();

        mockOrderItem1 = OrderItem.builder().id(orderItemId1).build();
        mockOrderItem2 = OrderItem.builder().id(orderItemId2).build();

        mockBatch = PrintBatch.builder()
                .id(batchId)
                .printedBy(mockEmployee)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .fileSystemPath(mockFilePath)
                .build();

        PrintBatchItem mockBatchItem1 = PrintBatchItem.builder().id(1L).printBatch(mockBatch).orderItem(mockOrderItem1).build();
        PrintBatchItem mockBatchItem2 = PrintBatchItem.builder().id(2L).printBatch(mockBatch).orderItem(mockOrderItem2).build();
        mockBatch.setItems(List.of(mockBatchItem1, mockBatchItem2));

        mockRequestDTO = new PrintBatchRequestDTO(List.of(orderItemId1, orderItemId2));
        mockListResponseDTO = mock(PrintBatchListResponseDTO.class);
        mockDetailResponseDTO = mock(PrintBatchDetailResponseDTO.class);
        mockResource = mock(Resource.class);
    }

    // --- Testes para findAll ---

    @Test
    @DisplayName("Deve retornar todos os lotes de impressão")
    void findAll_ShouldReturnBatchList_WhenRepositoryIsNotEmpty() {
        // Arrange
        when(printBatchRepository.findAllWithEmployee()).thenReturn(List.of(mockBatch));
        when(printBatchMapper.toListResponseDTO(mockBatch)).thenReturn(mockListResponseDTO);

        // Act
        List<PrintBatchListResponseDTO> result = printBatchService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(printBatchMapper, times(1)).toListResponseDTO(mockBatch);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há lotes")
    void findAll_ShouldReturnEmptyList_WhenRepositoryIsEmpty() {
        // Arrange
        when(printBatchRepository.findAllWithEmployee()).thenReturn(Collections.emptyList());

        // Act
        List<PrintBatchListResponseDTO> result = printBatchService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(printBatchMapper, never()).toListResponseDTO(any());
    }

    // --- Testes para findById ---

    @Test
    @DisplayName("Deve retornar DTO detalhado quando ID existir")
    void findById_ShouldReturnDetailDTO_WhenIdExists() {
        // Arrange
        when(printBatchRepository.findFullyLoadedBatchById(batchId)).thenReturn(Optional.of(mockBatch));
        when(printBatchMapper.toDetailResponseDTO(mockBatch)).thenReturn(mockDetailResponseDTO);

        // Act
        PrintBatchDetailResponseDTO result = printBatchService.findById(batchId);

        // Assert
        assertNotNull(result);
        verify(printBatchRepository, times(1)).findFullyLoadedBatchById(batchId);
        verify(printBatchMapper, times(1)).toDetailResponseDTO(mockBatch);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando ID não for encontrado")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(printBatchRepository.findFullyLoadedBatchById(batchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> printBatchService.findById(batchId));
    }

    // --- Testes para save ---

    @Test
    @DisplayName("Deve salvar novo lote, gerar PDF e retornar DTO detalhado")
    void save_ShouldCreateBatchProcessPdfAndReturnDetailDTO_WhenDataIsValid() throws IOException {
        // Arrange
        PrintBatch newBatch = PrintBatch.builder().id(batchId).printedBy(mockEmployee).build();
        byte[] pdfData = new byte[10];

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(printBatchRepository.save(any(PrintBatch.class))).thenReturn(newBatch);
        when(orderItemRepository.findAllById(anyList())).thenReturn(List.of(mockOrderItem1, mockOrderItem2));
        when(printBatchItemRepository.saveAllAndFlush(anyList())).thenReturn(mockBatch.getItems());

        when(printBatchRepository.findFullyLoadedBatchById(batchId)).thenReturn(Optional.of(newBatch));
        when(pdfGenerationService.generateBatchPdf(newBatch)).thenReturn(pdfData);
        when(fileStorageService.save(pdfData, batchId)).thenReturn(mockFilePath);
        when(printBatchMapper.toDetailResponseDTO(newBatch)).thenReturn(mockDetailResponseDTO);

        // Act
        PrintBatchDetailResponseDTO result = printBatchService.save(mockRequestDTO, employeeId);

        // Assert
        assertNotNull(result);
        assertEquals(mockFilePath, newBatch.getFileSystemPath());

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(printBatchRepository, times(2)).save(any(PrintBatch.class));
        verify(orderItemRepository, times(1)).findAllById(mockRequestDTO.getOrderItemIds());
        verify(printBatchItemRepository, times(1)).saveAllAndFlush(anyList());

        verify(pdfGenerationService, times(1)).generateBatchPdf(newBatch);
        verify(fileStorageService, times(1)).save(pdfData, batchId);

        verify(printBatchRepository, times(2)).findFullyLoadedBatchById(batchId);
        verify(printBatchMapper, times(1)).toDetailResponseDTO(newBatch);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando funcionário não for encontrado")
    void save_ShouldThrowException_WhenEmployeeNotFound() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> printBatchService.save(mockRequestDTO, employeeId));

        // Garante que nada foi salvo
        verify(printBatchRepository, never()).save(any());
        verify(printBatchItemRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("Deve gerar, salvar PDF e atualizar o path no lote")
    void generateAndStorePdf_ShouldGenerateAndSavePdf_WhenBatchExists() throws IOException {
        // Arrange
        byte[] pdfData = new byte[10];
        mockBatch.setFileSystemPath(null);

        when(printBatchRepository.findFullyLoadedBatchById(batchId)).thenReturn(Optional.of(mockBatch));
        when(pdfGenerationService.generateBatchPdf(mockBatch)).thenReturn(pdfData);
        when(fileStorageService.save(pdfData, batchId)).thenReturn(mockFilePath);

        // Act
        printBatchService.generateAndStorePdf(batchId);

        // Assert
        verify(printBatchRepository, times(1)).findFullyLoadedBatchById(batchId);
        verify(pdfGenerationService, times(1)).generateBatchPdf(mockBatch);
        verify(fileStorageService, times(1)).save(pdfData, batchId);
        verify(printBatchRepository, times(1)).save(mockBatch);
        assertEquals(mockFilePath, mockBatch.getFileSystemPath());
    }

    // --- Testes para download ---

    @Test
    @DisplayName("Deve retornar Map.Entry com nome e recurso quando o arquivo existir")
    void download_ShouldReturnFileResource_WhenFileExists() {
        // Arrange
        when(printBatchRepository.findById(batchId)).thenReturn(Optional.of(mockBatch));
        when(fileStorageService.loadAsResource(mockFilePath)).thenReturn(mockResource);
        when(fileStorageService.getFilename(mockFilePath)).thenReturn(mockFilename);

        // Act
        Map.Entry<String, Resource> result = printBatchService.download(batchId);

        // Assert
        assertNotNull(result);
        assertEquals(mockFilename, result.getKey());
        assertEquals(mockResource, result.getValue());
        verify(fileStorageService, times(1)).loadAsResource(mockFilePath);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao baixar se o lote não for encontrado")
    void download_ShouldThrowException_WhenIdNotFound() {
        // Arrange
        when(printBatchRepository.findById(batchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> printBatchService.download(batchId));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao baixar se o path for nulo")
    void download_ShouldThrowException_WhenFilePathIsNull() {
        // Arrange
        mockBatch.setFileSystemPath(null);
        when(printBatchRepository.findById(batchId)).thenReturn(Optional.of(mockBatch));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> printBatchService.download(batchId));

        assertTrue(exception.getMessage().contains("PDF file is not ready"));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao baixar se o path for vazio")
    void download_ShouldThrowException_WhenFilePathIsEmpty() {
        // Arrange
        mockBatch.setFileSystemPath("");
        when(printBatchRepository.findById(batchId)).thenReturn(Optional.of(mockBatch));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> printBatchService.download(batchId));

        assertTrue(exception.getMessage().contains("PDF file is not ready"));
    }
}