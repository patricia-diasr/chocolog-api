package com.chocolog.api.service;

import com.chocolog.api.dto.request.OrderItemPatchRequestDTO;
import com.chocolog.api.dto.request.OrderItemRequestDTO;
import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.mapper.OrderItemMapper;
import com.chocolog.api.model.*;
import com.chocolog.api.repository.FlavorRepository;
import com.chocolog.api.repository.OrderItemRepository;
import com.chocolog.api.repository.OrderRepository;
import com.chocolog.api.repository.SizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para OrderItemService")
public class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SizeRepository sizeRepository;
    @Mock
    private FlavorRepository flavorRepository;
    @Mock
    private ProductPriceService productPriceService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService orderItemService;

    private Order mockOrder;
    private OrderItem mockItem;
    private Flavor mockFlavor1;
    private Flavor mockFlavor2;
    private Size mockSize;
    private OrderItemRequestDTO mockRequestDTO;
    private OrderItemResponseDTO mockResponseDTO;

    private final Long customerId = 1L;
    private final Long orderId = 100L;
    private final Long itemId = 10L;
    private final Long flavor1Id = 1L;
    private final Long flavor2Id = 2L;
    private final Long sizeId = 5L;

    @BeforeEach
    void setUp() {
        mockFlavor1 = Flavor.builder().id(flavor1Id).name("Chocolate").build();
        mockFlavor2 = Flavor.builder().id(flavor2Id).name("Morango").build();
        mockSize = Size.builder().id(sizeId).name("Médio").build();

        mockOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.READY_FOR_PICKUP)
                .orderItems(new ArrayList<>()) // Lista mutável
                .build();

        mockItem = OrderItem.builder()
                .id(itemId)
                .order(mockOrder)
                .flavor1(mockFlavor1)
                .size(mockSize)
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(new BigDecimal("20.00"))
                .status(OrderStatus.READY_FOR_PICKUP)
                .onDemand(false)
                .build();

        mockOrder.getOrderItems().add(mockItem);

        mockRequestDTO = OrderItemRequestDTO.builder()
                .sizeId(sizeId)
                .flavor1Id(flavor1Id)
                .quantity(2)
                .build();

        mockResponseDTO = mock(OrderItemResponseDTO.class);
    }

    // --- Helpers de Mock ---
    private void mockFindOrderAndValidate() {
        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(mockOrder));
    }

    private void mockFindOrderItemOrFail() {
        when(orderItemRepository.findByIdAndOrder_Id(itemId, orderId)).thenReturn(Optional.of(mockItem));
    }

    private void mockMetadataFinds() {
        when(sizeRepository.findById(sizeId)).thenReturn(Optional.of(mockSize));
        when(flavorRepository.findById(flavor1Id)).thenReturn(Optional.of(mockFlavor1));
    }

    // --- Testes para findAll ---

    @Test
    @DisplayName("Deve retornar todos os itens (DTOs)")
    void findAll_ShouldReturnAllItems_WhenOnDemandIsNull() {
        // Arrange
        when(orderItemRepository.findAllAsDTO()).thenReturn(List.of(mockResponseDTO));

        // Act
        List<OrderItemResponseDTO> result = orderItemService.findAll(null);

        // Assert
        assertFalse(result.isEmpty());
        verify(orderItemRepository).findAllAsDTO();
    }

    @Test
    @DisplayName("Deve retornar apenas itens OnDemand quando flag é true")
    void findAll_ShouldReturnOnDemandItems_WhenOnDemandIsTrue() {
        // Arrange
        when(orderItemRepository.findAllAsDTOByOnDemandTrue()).thenReturn(List.of(mockResponseDTO));

        // Act
        List<OrderItemResponseDTO> result = orderItemService.findAll(true);

        // Assert
        assertFalse(result.isEmpty());
        verify(orderItemRepository).findAllAsDTOByOnDemandTrue();
    }

    // --- Testes para addItemToOrder ---

    @Test
    @DisplayName("Deve adicionar item, calcular preço e debitar estoque (Ready)")
    void addItemToOrder_ShouldAddItemAndAdjustStock_WhenItemIsNotOnDemand() {
        // Arrange
        mockFindOrderAndValidate();
        mockMetadataFinds();
        when(productPriceService.calculateUnitPrice(mockSize, mockFlavor1, null)).thenReturn(new BigDecimal("10.00"));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.addItemToOrder(customerId, orderId, mockRequestDTO);

        // Assert
        // Verifica se estoque foi debitado (-2)
        verify(orderService).adjustRemainingStock(mockFlavor1, mockSize, -2);
        verify(orderService).recalculateOrderCharge(orderId);
        verify(orderService).recalculateAndSaveOrderStatus(orderId);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertFalse(itemCaptor.getValue().getOnDemand());
        assertEquals(OrderStatus.READY_FOR_PICKUP, itemCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve adicionar item PENDING e NÃO debitar estoque se for OnDemand (ex: 2 sabores)")
    void addItemToOrder_ShouldAddItemPendingAndNotAdjustStock_WhenItemIsOnDemand() {
        // Arrange
        mockRequestDTO.setFlavor2Id(flavor2Id); // 2 sabores = OnDemand

        mockFindOrderAndValidate();
        when(sizeRepository.findById(sizeId)).thenReturn(Optional.of(mockSize));
        when(flavorRepository.findById(flavor1Id)).thenReturn(Optional.of(mockFlavor1));
        when(flavorRepository.findById(flavor2Id)).thenReturn(Optional.of(mockFlavor2));

        when(productPriceService.calculateUnitPrice(mockSize, mockFlavor1, mockFlavor2)).thenReturn(new BigDecimal("15.00"));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.addItemToOrder(customerId, orderId, mockRequestDTO);

        // Assert
        // Verifica que NÃO houve ajuste de estoque para item novo pending
        verify(orderService, never()).adjustRemainingStock(any(), any(), anyInt());

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertTrue(itemCaptor.getValue().getOnDemand());
        assertEquals(OrderStatus.PENDING, itemCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar item em pedido COMPLETED")
    void addItemToOrder_ShouldThrowException_WhenOrderIsCompleted() {
        // Arrange
        mockOrder.setStatus(OrderStatus.COMPLETED);
        mockFindOrderAndValidate();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> orderItemService.addItemToOrder(customerId, orderId, mockRequestDTO));
    }

    // --- Testes para updateOrderItem ---

    @Test
    @DisplayName("Deve atualizar quantidade e ajustar a diferença no estoque (Ready -> Ready)")
    void updateOrderItem_ShouldUpdateQuantityAndAdjustStockDifference() {
        // Arrange
        mockFindOrderAndValidate();
        mockFindOrderItemOrFail(); // mockItem tem Qtd: 2

        // Patch muda qtd para 3 (Diferença: -1 no estoque)
        OrderItemPatchRequestDTO patchDTO = OrderItemPatchRequestDTO.builder().quantity(3).build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.updateOrderItem(customerId, orderId, itemId, patchDTO);

        // Assert
        // originalQuantity(2) - newQuantity(3) = -1
        verify(orderService).adjustRemainingStock(mockFlavor1, mockSize, -1);
        assertEquals(3, mockItem.getQuantity());
        verify(orderService).recalculateOrderCharge(orderId);
    }

    @Test
    @DisplayName("Deve atualizar de Ready para Pending (OnDemand) e devolver estoque")
    void updateOrderItem_ShouldReturnStock_WhenChangingFromReadyToPending() {
        // Arrange
        mockFindOrderAndValidate();
        mockFindOrderItemOrFail(); // Item é READY, Qtd: 2, OnDemand: false

        OrderItemPatchRequestDTO patchDTO = OrderItemPatchRequestDTO.builder()
                .notes("Capricha no recheio")
                .build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.updateOrderItem(customerId, orderId, itemId, patchDTO);

        // Assert
        verify(orderService).adjustRemainingStock(mockFlavor1, mockSize, 2);
        assertTrue(mockItem.getOnDemand());
        assertEquals(OrderStatus.PENDING, mockItem.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar de Pending para Ready (Not OnDemand) e consumir estoque")
    void updateOrderItem_ShouldConsumeStock_WhenChangingFromPendingToReady() {
        // Arrange
        mockItem.setStatus(OrderStatus.PENDING);
        mockItem.setOnDemand(true);
        mockItem.setNotes("Nota antiga");

        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        OrderItemPatchRequestDTO patchDTO = OrderItemPatchRequestDTO.builder()
                .notes("")
                .build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.updateOrderItem(customerId, orderId, itemId, patchDTO);

        // Assert
        verify(orderService).adjustRemainingStock(mockFlavor1, mockSize, -2);
        assertFalse(mockItem.getOnDemand());
        assertEquals(OrderStatus.READY_FOR_PICKUP, mockItem.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar status manualmente e ajustar TotalStock se for para COMPLETED")
    void updateOrderItem_ShouldAdjustTotalStock_WhenStatusManuallyChangedToCompleted() {
        // Arrange
        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        OrderItemPatchRequestDTO patchDTO = OrderItemPatchRequestDTO.builder()
                .status("COMPLETED")
                .build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.updateOrderItem(customerId, orderId, itemId, patchDTO);

        // Assert
        assertEquals(OrderStatus.COMPLETED, mockItem.getStatus());
        verify(orderService).adjustTotalStock(mockFlavor1, mockSize, -2);
    }

    @Test
    @DisplayName("Deve recalcular preço se sabor mudar")
    void updateOrderItem_ShouldRecalculatePrice_WhenFlavorChanges() {
        // Arrange
        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        OrderItemPatchRequestDTO patchDTO = OrderItemPatchRequestDTO.builder().flavor1Id(flavor2Id).build();

        when(flavorRepository.findById(flavor2Id)).thenReturn(Optional.of(mockFlavor2));
        when(productPriceService.calculateUnitPrice(mockSize, mockFlavor2, null)).thenReturn(new BigDecimal("12.00"));

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemMapper.toResponseDTO(mockItem)).thenReturn(mockResponseDTO);

        // Act
        orderItemService.updateOrderItem(customerId, orderId, itemId, patchDTO);

        // Assert
        assertEquals(mockFlavor2, mockItem.getFlavor1());
        assertEquals(new BigDecimal("12.00"), mockItem.getUnitPrice());
    }

    // --- Testes para deleteOrderItem ---

    @Test
    @DisplayName("Deve deletar item Ready e devolver estoque")
    void deleteOrderItem_ShouldReturnStockAndDelete_WhenItemIsReady() {
        // Arrange
        mockOrder.getOrderItems().add(new OrderItem());

        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        // Act
        orderItemService.deleteOrderItem(customerId, orderId, itemId);

        // Assert
        verify(orderService).adjustRemainingStock(mockFlavor1, mockSize, 2);
        verify(orderItemRepository).delete(mockItem);
        verify(orderService).recalculateOrderCharge(orderId);
        assertFalse(mockOrder.getOrderItems().contains(mockItem));
    }

    @Test
    @DisplayName("Deve deletar item Pending e NÃO mexer no estoque")
    void deleteOrderItem_ShouldNotChangeStock_WhenItemIsPending() {
        // Arrange
        mockOrder.getOrderItems().add(new OrderItem());
        mockItem.setStatus(OrderStatus.PENDING);
        mockItem.setOnDemand(true);

        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        // Act
        orderItemService.deleteOrderItem(customerId, orderId, itemId);

        // Assert
        verify(orderService, never()).adjustRemainingStock(any(), any(), anyInt());
        verify(orderItemRepository).delete(mockItem);
    }

    @Test
    @DisplayName("Deve lançar exceção se pedido tiver apenas 1 item")
    void deleteOrderItem_ShouldThrowException_WhenOrderHasOnlyOneItem() {
        // Arrange
        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> orderItemService.deleteOrderItem(customerId, orderId, itemId));

        verify(orderItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se item estiver COMPLETED")
    void deleteOrderItem_ShouldThrowException_WhenItemIsCompleted() {
        // Arrange
        mockOrder.getOrderItems().add(new OrderItem());
        mockItem.setStatus(OrderStatus.COMPLETED);

        mockFindOrderAndValidate();
        mockFindOrderItemOrFail();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> orderItemService.deleteOrderItem(customerId, orderId, itemId));
    }
}