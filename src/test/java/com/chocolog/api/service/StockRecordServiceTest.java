package com.chocolog.api.service;

import com.chocolog.api.dto.request.StockRecordRequestDTO;
import com.chocolog.api.dto.response.StockRecordResponseDTO;
import com.chocolog.api.mapper.StockRecordMapper;
import com.chocolog.api.model.*;
import com.chocolog.api.repository.FlavorRepository;
import com.chocolog.api.repository.SizeRepository;
import com.chocolog.api.repository.StockRecordRepository;
import com.chocolog.api.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para StockRecordService")
public class StockRecordServiceTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockRecordRepository stockRecordRepository;
    @Mock
    private FlavorRepository flavorRepository;
    @Mock
    private SizeRepository sizeRepository;
    @Mock
    private StockRecordMapper stockRecordMapper;

    @InjectMocks
    private StockRecordService stockRecordService;

    private Flavor mockFlavor;
    private Size mockSize;
    private Stock mockStock;
    private StockRecord mockStockRecord;
    private StockRecordRequestDTO mockInboundRequestDTO;
    private StockRecordRequestDTO mockOutboundRequestDTO;
    private StockRecordResponseDTO mockResponseDTO;

    private final Long flavorId = 1L;
    private final Long sizeId = 1L;

    @BeforeEach
    void setUp() {
        mockFlavor = Flavor.builder().id(flavorId).name("Sensação").build();
        mockSize = Size.builder().id(sizeId).name("350g").build();

        mockStock = Stock.builder()
                .id(1L)
                .flavor(mockFlavor)
                .size(mockSize)
                .totalQuantity(10)
                .remainingQuantity(10)
                .build();

        mockInboundRequestDTO = StockRecordRequestDTO.builder()
                .flavorId(flavorId)
                .sizeId(sizeId)
                .quantity(5)
                .movementType("INBOUND")
                .build();

        mockOutboundRequestDTO = StockRecordRequestDTO.builder()
                .flavorId(flavorId)
                .sizeId(sizeId)
                .quantity(5)
                .movementType("OUTBOUND")
                .build();

        mockStockRecord = StockRecord.builder()
                .id(1L)
                .flavor(mockFlavor)
                .size(mockSize)
                .quantity(5)
                .movementType(StockMovement.INBOUND)
                .productionDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        mockResponseDTO = StockRecordResponseDTO.builder()
                .id(1L)
                .flavorName(mockFlavor.getName())
                .sizeName(mockSize.getName())
                .quantity(5)
                .movementType("INBOUND")
                .productionDate(mockStockRecord.getProductionDate())
                .expirationDate(mockStockRecord.getExpirationDate())
                .build();
    }

    // --- Métodos Privados Auxiliares ---
    private void mockFindFlavorAndSize() {
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));
        when(sizeRepository.findById(sizeId)).thenReturn(Optional.of(mockSize));
    }

    // --- Testes para findAll ---

    @Test
    @DisplayName("Deve retornar todos os registros de estoque quando a lista não está vazia")
    void findAll_ShouldReturnStockRecords_WhenRepositoryIsNotEmpty() {
        // Arrange
        when(stockRecordRepository.findAll()).thenReturn(List.of(mockStockRecord));
        when(stockRecordMapper.toResponseDTO(mockStockRecord)).thenReturn(mockResponseDTO);

        // Act
        List<StockRecordResponseDTO> result = stockRecordService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(stockRecordMapper, times(1)).toResponseDTO(mockStockRecord);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há registros")
    void findAll_ShouldReturnEmptyList_WhenRepositoryIsEmpty() {
        // Arrange
        when(stockRecordRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<StockRecordResponseDTO> result = stockRecordService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(stockRecordMapper, never()).toResponseDTO(any());
    }

    // --- Testes para save ---

    @Test
    @DisplayName("Deve salvar registro INBOUND e atualizar estoque existente")
    void save_ShouldSaveInboundRecord_WhenStockExists() {
        // Arrange
        mockFindFlavorAndSize();
        when(stockRepository.findByFlavorAndSize(mockFlavor, mockSize)).thenReturn(Optional.of(mockStock));
        when(stockRecordMapper.toEntity(mockInboundRequestDTO)).thenReturn(new StockRecord());

        ArgumentCaptor<StockRecord> recordCaptor = ArgumentCaptor.forClass(StockRecord.class);
        when(stockRecordRepository.save(recordCaptor.capture())).thenReturn(mockStockRecord);
        when(stockRecordMapper.toResponseDTO(mockStockRecord)).thenReturn(mockResponseDTO);

        // Act
        StockRecordResponseDTO result = stockRecordService.save(mockInboundRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(15, mockStock.getTotalQuantity());
        assertEquals(15, mockStock.getRemainingQuantity());
        verify(stockRepository, times(1)).save(mockStock);

        StockRecord capturedRecord = recordCaptor.getValue();
        assertEquals(mockFlavor, capturedRecord.getFlavor());
        assertEquals(mockSize, capturedRecord.getSize());
        assertEquals(StockMovement.INBOUND, capturedRecord.getMovementType());
        assertNotNull(capturedRecord.getProductionDate());
        assertNotNull(capturedRecord.getExpirationDate());
    }

    @Test
    @DisplayName("Deve salvar registro INBOUND e criar novo estoque se não existir")
    void save_ShouldSaveInboundRecord_WhenStockDoesNotExist() {
        // Arrange
        mockFindFlavorAndSize();
        when(stockRepository.findByFlavorAndSize(mockFlavor, mockSize)).thenReturn(Optional.empty());
        when(stockRecordMapper.toEntity(mockInboundRequestDTO)).thenReturn(new StockRecord());

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        when(stockRepository.save(stockCaptor.capture())).thenReturn(mockStock);

        when(stockRecordRepository.save(any(StockRecord.class))).thenReturn(mockStockRecord);
        when(stockRecordMapper.toResponseDTO(mockStockRecord)).thenReturn(mockResponseDTO);

        // Act
        StockRecordResponseDTO result = stockRecordService.save(mockInboundRequestDTO);

        // Assert
        assertNotNull(result);

        Stock newStock = stockCaptor.getValue();
        assertEquals(5, newStock.getTotalQuantity());
        assertEquals(5, newStock.getRemainingQuantity());
        assertEquals(mockFlavor, newStock.getFlavor());
        assertEquals(mockSize, newStock.getSize());
        verify(stockRepository, times(1)).save(newStock);
        verify(stockRecordRepository, times(1)).save(any(StockRecord.class));
    }

    @Test
    @DisplayName("Deve salvar registro OUTBOUND e atualizar estoque existente")
    void save_ShouldSaveOutboundRecord_WhenStockIsSufficient() {
        // Arrange
        mockFindFlavorAndSize();
        when(stockRepository.findByFlavorAndSize(mockFlavor, mockSize)).thenReturn(Optional.of(mockStock));
        when(stockRecordMapper.toEntity(mockOutboundRequestDTO)).thenReturn(new StockRecord());

        ArgumentCaptor<StockRecord> recordCaptor = ArgumentCaptor.forClass(StockRecord.class);
        when(stockRecordRepository.save(recordCaptor.capture())).thenReturn(mockStockRecord);
        when(stockRecordMapper.toResponseDTO(mockStockRecord)).thenReturn(mockResponseDTO);

        // Act
        StockRecordResponseDTO result = stockRecordService.save(mockOutboundRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(5, mockStock.getTotalQuantity());
        assertEquals(5, mockStock.getRemainingQuantity());
        verify(stockRepository, times(1)).save(mockStock);

        StockRecord capturedRecord = recordCaptor.getValue();
        assertEquals(StockMovement.OUTBOUND, capturedRecord.getMovementType());
        assertNotNull(capturedRecord.getProductionDate());
        assertNull(capturedRecord.getExpirationDate());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para OUTBOUND com estoque insuficiente")
    void save_ShouldThrowIllegalArgumentException_WhenOutboundStockIsInsufficient() {
        // Arrange
        mockStock.setTotalQuantity(3);
        mockStock.setRemainingQuantity(3);

        mockFindFlavorAndSize();
        when(stockRepository.findByFlavorAndSize(mockFlavor, mockSize)).thenReturn(Optional.of(mockStock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> stockRecordService.save(mockOutboundRequestDTO));

        assertEquals(3, mockStock.getTotalQuantity());
        verify(stockRepository, never()).save(any());
        verify(stockRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para OUTBOUND se estoque não existir")
    void save_ShouldThrowIllegalArgumentException_WhenOutboundStockDoesNotExist() {
        // Arrange
        mockFindFlavorAndSize();
        when(stockRepository.findByFlavorAndSize(mockFlavor, mockSize)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> stockRecordService.save(mockOutboundRequestDTO));

        verify(stockRepository, never()).save(any());
        verify(stockRecordRepository, never()).save(any());
    }

    // --- Testes de Exceção ---

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se Flavor não for encontrado")
    void save_ShouldThrowEntityNotFoundException_WhenFlavorNotFound() {
        // Arrange
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> stockRecordService.save(mockInboundRequestDTO));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se Size não for encontrado")
    void save_ShouldThrowEntityNotFoundException_WhenSizeNotFound() {
        // Arrange
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));
        when(sizeRepository.findById(sizeId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> stockRecordService.save(mockInboundRequestDTO));
    }
}