package com.chocolog.api.service;

import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.ProductPrice;
import com.chocolog.api.model.Size;
import com.chocolog.api.repository.ProductPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para ProductPriceService")
public class ProductPriceServiceTest {

    @Mock
    private ProductPriceRepository productPriceRepository;

    @InjectMocks
    private ProductPriceService productPriceService;

    private Flavor mockFlavor1;
    private Flavor mockFlavor2;
    private Size mockSize;
    private ProductPrice mockPrice1;
    private ProductPrice mockPrice2;

    @BeforeEach
    void setUp() {
        mockFlavor1 = Flavor.builder()
                .id(1L)
                .name("Brigadeiro")
                .build();

        mockFlavor2 = Flavor.builder()
                .id(2L)
                .name("Ninho")
                .build();

        mockSize = Size.builder()
                .id(1L)
                .name("500g")
                .build();

        mockPrice1 = ProductPrice.builder()
                .id(1L)
                .flavor(mockFlavor1)
                .size(mockSize)
                .salePrice(new BigDecimal("10.00"))
                .build();

        mockPrice2 = ProductPrice.builder()
                .id(2L)
                .flavor(mockFlavor2)
                .size(mockSize)
                .salePrice(new BigDecimal("12.00"))
                .build();
    }

    // --- Testes para calculateUnitPrice ---

    @Test
    @DisplayName("Deve retornar o preço do primeiro sabor quando o segundo sabor for nulo")
    void calculateUnitPrice_ShouldReturnPriceOfFirstFlavor_WhenSecondFlavorIsNull() {
        // Arrange
        when(productPriceRepository.findByFlavorAndSize(mockFlavor1, mockSize))
                .thenReturn(Optional.of(mockPrice1));

        // Act
        BigDecimal result = productPriceService.calculateUnitPrice(mockSize, mockFlavor1, null);

        // Assert
        assertEquals(new BigDecimal("10.00"), result);
        verify(productPriceRepository, times(1)).findByFlavorAndSize(mockFlavor1, mockSize);
        verify(productPriceRepository, never()).findByFlavorAndSize(mockFlavor2, mockSize);
    }

    @Test
    @DisplayName("Deve retornar a média dos preços quando dois sabores forem fornecidos")
    void calculateUnitPrice_ShouldReturnAveragePrice_WhenTwoFlavorsAreProvided() {
        // Arrange
        when(productPriceRepository.findByFlavorAndSize(mockFlavor1, mockSize))
                .thenReturn(Optional.of(mockPrice1));
        when(productPriceRepository.findByFlavorAndSize(mockFlavor2, mockSize))
                .thenReturn(Optional.of(mockPrice2));

        BigDecimal expectedAverage = new BigDecimal("11.00");

        // Act
        BigDecimal result = productPriceService.calculateUnitPrice(mockSize, mockFlavor1, mockFlavor2);

        // Assert
        assertEquals(expectedAverage, result);
        verify(productPriceRepository, times(1)).findByFlavorAndSize(mockFlavor1, mockSize);
        verify(productPriceRepository, times(1)).findByFlavorAndSize(mockFlavor2, mockSize);
    }

    @Test
    @DisplayName("Deve arredondar para cima (HALF_UP) ao calcular a média")
    void calculateUnitPrice_ShouldRoundUp_WhenAverageHasMoreThanTwoDecimals() {
        // Arrange
        mockPrice1.setSalePrice(new BigDecimal("10.00"));
        mockPrice2.setSalePrice(new BigDecimal("10.25"));

        when(productPriceRepository.findByFlavorAndSize(mockFlavor1, mockSize))
                .thenReturn(Optional.of(mockPrice1));
        when(productPriceRepository.findByFlavorAndSize(mockFlavor2, mockSize))
                .thenReturn(Optional.of(mockPrice2));

        BigDecimal expectedAverage = new BigDecimal("10.13");

        // Act
        BigDecimal result = productPriceService.calculateUnitPrice(mockSize, mockFlavor1, mockFlavor2);

        // Assert
        assertEquals(expectedAverage, result);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o preço do primeiro sabor não for encontrado")
    void calculateUnitPrice_ShouldThrowException_WhenFirstFlavorPriceNotFound() {
        // Arrange
        when(productPriceRepository.findByFlavorAndSize(mockFlavor1, mockSize))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> productPriceService.calculateUnitPrice(mockSize, mockFlavor1, null));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o preço do segundo sabor não for encontrado")
    void calculateUnitPrice_ShouldThrowException_WhenSecondFlavorPriceNotFound() {
        // Arrange
        when(productPriceRepository.findByFlavorAndSize(mockFlavor1, mockSize))
                .thenReturn(Optional.of(mockPrice1));
        when(productPriceRepository.findByFlavorAndSize(mockFlavor2, mockSize))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> productPriceService.calculateUnitPrice(mockSize, mockFlavor1, mockFlavor2));

        verify(productPriceRepository, times(1)).findByFlavorAndSize(mockFlavor1, mockSize);
    }
}