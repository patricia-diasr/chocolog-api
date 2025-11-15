package com.chocolog.api.service;

import com.chocolog.api.dto.request.FlavorPatchRequestDTO;
import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.request.PriceRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.dto.response.FlavorSizeResponseDTO;
import com.chocolog.api.mapper.FlavorMapper;
import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.ProductPrice;
import com.chocolog.api.model.Size;
import com.chocolog.api.model.Stock;
import com.chocolog.api.repository.FlavorRepository;
import com.chocolog.api.repository.ProductPriceRepository;
import com.chocolog.api.repository.SizeRepository;
import com.chocolog.api.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para FlavorService")
public class FlavorServiceTest {

    @Mock private SizeRepository sizeRepository;
    @Mock private ProductPriceRepository productPriceRepository;
    @Mock private StockRepository stockRepository;
    @Mock private FlavorRepository flavorRepository;
    @Mock private FlavorMapper flavorMapper;

    @InjectMocks
    private FlavorService flavorService;

    private Flavor mockFlavor;
    private Size mockSize;
    private ProductPrice mockProductPrice;
    private Stock mockStock;
    private FlavorResponseDTO mockResponseDTO;

    @BeforeEach
    void setUp() {
        mockFlavor = Flavor.builder().id(1L).name("Chocolate Blend").build();
        mockSize = Size.builder().id(10L).name("Pequeno").build();

        mockProductPrice = ProductPrice.builder()
                .flavor(mockFlavor)
                .size(mockSize)
                .salePrice(new BigDecimal("10.00"))
                .costPrice(new BigDecimal("5.00"))
                .build();

        mockStock = Stock.builder()
                .flavor(mockFlavor)
                .size(mockSize)
                .totalQuantity(100)
                .remainingQuantity(80)
                .build();

        mockResponseDTO = new FlavorResponseDTO(1L, "Chocolate Blend", null);
    }

    private void mockBuildFlavorResponseDTO() {
        when(flavorMapper.toResponseDTO(any(Flavor.class))).thenReturn(mockResponseDTO);
        when(sizeRepository.findAll()).thenReturn(List.of(mockSize));
        when(productPriceRepository.findByFlavorAndSize(any(Flavor.class), any(Size.class)))
                .thenReturn(Optional.of(mockProductPrice));
        when(stockRepository.findByFlavorAndSize(any(Flavor.class), any(Size.class)))
                .thenReturn(Optional.of(mockStock));
    }

    // --- Testes para findAll ---
    @Test
    @DisplayName("Deve retornar todos os sabores e suas informações de tamanho/preço/estoque")
    void findAll_ShouldReturnFlavors_WhenRepositoryIsNotEmpty() {
        // Arrange
        List<Flavor> flavors = List.of(mockFlavor);
        when(flavorRepository.findAll()).thenReturn(flavors);
        mockBuildFlavorResponseDTO();

        // Act
        List<FlavorResponseDTO> result = flavorService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Chocolate Blend", result.get(0).getName());
        assertNotNull(result.get(0).getSizes());
        verify(flavorRepository, times(1)).findAll();
        verify(sizeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há sabores")
    void findAll_ShouldReturnEmptyList_WhenRepositoryIsEmpty() {
        // Arrange
        when(flavorRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<FlavorResponseDTO> result = flavorService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(sizeRepository, never()).findAll();
    }

    // --- Testes para findById ---
    @Test
    @DisplayName("Deve retornar FlavorResponseDTO completo quando o ID for encontrado")
    void findById_ShouldReturnFlavorResponseDTO_WhenIdExists() {
        // Arrange
        when(flavorRepository.findById(1L)).thenReturn(Optional.of(mockFlavor));
        mockBuildFlavorResponseDTO();

        // Act
        FlavorResponseDTO result = flavorService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Chocolate Blend", result.getName());
        verify(flavorRepository, times(1)).findById(1L);

        List<FlavorSizeResponseDTO> sizes = result.getSizes();
        assertFalse(sizes.isEmpty());
        assertEquals(new BigDecimal("10.00"), sizes.get(0).getSalePrice());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(flavorRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> flavorService.findById(2L));
    }

    // --- Testes para save ---
    @Test
    @DisplayName("Deve salvar novo sabor com preços e estoque quando dados válidos são fornecidos")
    void save_ShouldSaveFlavorPricesAndStock_WhenValidData() {
        // Arrange
        FlavorRequestDTO requestDTO = FlavorRequestDTO.builder()
                .name("Morango Cremoso")
                .prices(List.of(
                        PriceRequestDTO.builder().sizeId(10L).salePrice(new BigDecimal("12.00")).costPrice(new BigDecimal("6.00")).build()
                ))
                .build();

        Flavor flavorToSave = Flavor.builder().name("Morango Cremoso").build();
        Flavor savedFlavor = Flavor.builder().id(2L).name("Morango Cremoso").build();

        when(flavorRepository.findByName("Morango Cremoso")).thenReturn(Optional.empty());
        when(flavorMapper.toEntity(requestDTO)).thenReturn(flavorToSave);
        when(flavorRepository.save(flavorToSave)).thenReturn(savedFlavor);
        when(sizeRepository.findById(10L)).thenReturn(Optional.of(mockSize));

        when(flavorRepository.findById(2L)).thenReturn(Optional.of(savedFlavor));
        mockBuildFlavorResponseDTO();

        // Act
        FlavorResponseDTO result = flavorService.save(requestDTO);

        // Assert
        assertNotNull(result);
        verify(flavorRepository, times(1)).save(flavorToSave);

        verify(productPriceRepository, times(1)).save(any(ProductPrice.class));
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o nome já está em uso ao salvar")
    void save_ShouldThrowException_WhenNameIsAlreadyInUse() {
        // Arrange
        FlavorRequestDTO requestDTO = FlavorRequestDTO.builder().name("Chocolate Blend").build();
        when(flavorRepository.findByName("Chocolate Blend")).thenReturn(Optional.of(mockFlavor));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> flavorService.save(requestDTO));
        verify(flavorRepository, never()).save(any());
        verify(productPriceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando SizeId não é encontrado ao salvar preços")
    void save_ShouldThrowException_WhenSizeIdNotFound() {
        // Arrange
        FlavorRequestDTO requestDTO = FlavorRequestDTO.builder()
                .name("Morango")
                .prices(List.of(
                        PriceRequestDTO.builder().sizeId(99L).salePrice(BigDecimal.TEN).costPrice(BigDecimal.ONE).build()
                ))
                .build();

        Flavor flavorToSave = Flavor.builder().name("Morango").build();

        when(flavorRepository.findByName("Morango")).thenReturn(Optional.empty());
        when(flavorMapper.toEntity(requestDTO)).thenReturn(flavorToSave);
        when(flavorRepository.save(flavorToSave)).thenReturn(mockFlavor);
        when(sizeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> flavorService.save(requestDTO));
        verify(productPriceRepository, never()).save(any());
        verify(stockRepository, never()).save(any());
    }

    // --- Testes para update ---
    @Test
    @DisplayName("Deve atualizar apenas o nome do sabor")
    void update_ShouldUpdateFlavorName_WhenNameIsProvided() {
        // Arrange
        Long flavorId = 1L;
        FlavorPatchRequestDTO requestDTO = FlavorPatchRequestDTO.builder()
                .name("Chocolate Duplo")
                .build();

        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));
        when(flavorRepository.findByName("Chocolate Duplo")).thenReturn(Optional.empty());

        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));
        mockBuildFlavorResponseDTO();

        // Act
        flavorService.update(flavorId, requestDTO);

        // Assert
        assertEquals("Chocolate Duplo", mockFlavor.getName());
        verify(flavorRepository, times(2)).findById(flavorId);
        verify(flavorRepository, times(1)).findByName("Chocolate Duplo");
        verify(productPriceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o nome novo já está em uso no update")
    void update_ShouldThrowException_WhenNewNameIsAlreadyInUse() {
        // Arrange
        Long flavorId = 1L;
        String newName = "Baunilha";
        FlavorPatchRequestDTO requestDTO = FlavorPatchRequestDTO.builder().name(newName).build();

        Flavor otherFlavor = Flavor.builder().id(2L).name(newName).build();

        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));
        when(flavorRepository.findByName(newName)).thenReturn(Optional.of(otherFlavor));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> flavorService.update(flavorId, requestDTO));
        verify(flavorRepository, times(1)).findByName(newName);
        verify(productPriceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado no update")
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long flavorId = 99L;
        FlavorPatchRequestDTO requestDTO = FlavorPatchRequestDTO.builder().name("Teste").build();
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> flavorService.update(flavorId, requestDTO));
        verify(flavorRepository, times(1)).findById(flavorId);
        verify(productPriceRepository, never()).save(any());
    }

    // --- Testes para deleteById ---
    @Test
    @DisplayName("Deve deletar o sabor quando o ID é encontrado")
    void deleteById_ShouldDeleteFlavor_WhenIdExists() {
        // Arrange
        Long flavorId = 1L;
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.of(mockFlavor));

        // Act
        flavorService.deleteById(flavorId);

        // Assert
        verify(flavorRepository, times(1)).findById(flavorId);
        verify(flavorRepository, times(1)).delete(mockFlavor);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado no delete")
    void deleteById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long flavorId = 99L;
        when(flavorRepository.findById(flavorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> flavorService.deleteById(flavorId));
        verify(flavorRepository, never()).delete(any());
    }
}