package com.chocolog.api.service;

import com.chocolog.api.dto.request.OrderItemPatchRequestDTO;
import com.chocolog.api.dto.request.OrderItemRequestDTO;
import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.mapper.OrderItemMapper;
import com.chocolog.api.model.*;
import com.chocolog.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final SizeRepository sizeRepository;
    private final FlavorRepository flavorRepository;
    private final ProductPriceService productPriceService;
    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderItemResponseDTO addItemToOrder(Long customerId, Long orderId, OrderItemRequestDTO itemDTO) {
        Order order = findOrderAndValidate(orderId, customerId);

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add items to a COMPLETED or CANCELLED order.");
        }

        OrderItem item = buildNewOrderItem(itemDTO, order);
        OrderItem savedItem = orderItemRepository.save(item);

        orderService.recalculateOrderCharge(orderId);
        orderService.recalculateAndSaveOrderStatus(orderId);

        return orderItemMapper.toResponseDTO(savedItem);
    }

    @Transactional
    public OrderItemResponseDTO updateOrderItem(Long customerId, Long orderId, Long itemId, OrderItemPatchRequestDTO patchDTO) {
        Order order = findOrderAndValidate(orderId, customerId);
        OrderItem item = findOrderItemOrFail(itemId, orderId);

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot edit items in a COMPLETED or CANCELLED order.");
        }

        if (item.getStatus() == OrderStatus.COMPLETED || item.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot edit a COMPLETED or CANCELLED item.");
        }

        boolean wasOnDemand = item.getOnDemand();
        int originalQuantity = item.getQuantity();
        Flavor originalFlavor1 = item.getFlavor1();
        Size originalSize = item.getSize();
        OrderStatus originalStatus = item.getStatus();

        boolean priceChanged = applyPatch(item, patchDTO);
        if (priceChanged) {
            recalculatePrices(item);
        }

        updateOnDemandStatus(item);
        boolean isNowOnDemand = item.getOnDemand();
        
        if (wasOnDemand != isNowOnDemand) {
            if (!wasOnDemand && isNowOnDemand && originalStatus == OrderStatus.READY_FOR_PICKUP) {
                item.setStatus(OrderStatus.PENDING);
            } else if (wasOnDemand && !isNowOnDemand && originalStatus == OrderStatus.PENDING) {
                item.setStatus(OrderStatus.READY_FOR_PICKUP);
            }
        } else if (patchDTO.getStatus() != null) {
            handleManualStatusUpdate(item, OrderStatus.valueOf(patchDTO.getStatus()));
        }

        updateStockForModification(wasOnDemand, isNowOnDemand, originalQuantity, originalFlavor1, originalSize, item);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        OrderItem updatedItem = orderItemRepository.save(item);
        orderService.recalculateOrderCharge(orderId);
        orderService.recalculateAndSaveOrderStatus(orderId);

        return orderItemMapper.toResponseDTO(updatedItem);
    }

    @Transactional
    public void deleteOrderItem(Long customerId, Long orderId, Long itemId) {
        Order order = findOrderAndValidate(orderId, customerId);
        OrderItem itemToDelete = findOrderItemOrFail(itemId, orderId);

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete items from a COMPLETED or CANCELLED order.");
        }

        if (itemToDelete.getStatus() != OrderStatus.PENDING && itemToDelete.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("Cannot delete an item that is not PENDING or READY_FOR_PICKUP.");
        }

        if (order.getOrderItems().size() <= 1) {
            throw new IllegalArgumentException("An order must have at least one item.");
        }

        if (!itemToDelete.getOnDemand()) {
            orderService.adjustRemainingStock(itemToDelete.getFlavor1(), itemToDelete.getSize(), itemToDelete.getQuantity());
        }

        order.getOrderItems().remove(itemToDelete);
        orderItemRepository.delete(itemToDelete);

        orderService.recalculateOrderCharge(orderId);
        orderService.recalculateAndSaveOrderStatus(orderId);
    }

    private void handleManualStatusUpdate(OrderItem item, OrderStatus newStatus) {
        OrderStatus currentStatus = item.getStatus();
        if (currentStatus == newStatus) return;

        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.READY_FOR_PICKUP) {
            if (!item.getOnDemand()) {
                throw new IllegalStateException("A non-onDemand item cannot be manually moved to READY_FOR_PICKUP, it starts as such.");
            }
            item.setStatus(newStatus);
        }

        else if (currentStatus == OrderStatus.READY_FOR_PICKUP && newStatus == OrderStatus.COMPLETED) {
            item.setStatus(newStatus);
            if (!item.getOnDemand()) {
                orderService.adjustTotalStock(item.getFlavor1(), item.getSize(), -item.getQuantity());
            }
        }

        else {
            throw new IllegalArgumentException("Invalid manual status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private OrderItem buildNewOrderItem(OrderItemRequestDTO itemDTO, Order order) {
        Size size = findSizeOrFail(itemDTO.getSizeId());
        Flavor flavor1 = findFlavorOrFail(itemDTO.getFlavor1Id());
        Flavor flavor2 = itemDTO.getFlavor2Id() != null ? findFlavorOrFail(itemDTO.getFlavor2Id()) : null;
        BigDecimal unitPrice = productPriceService.calculateUnitPrice(size, flavor1, flavor2);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setSize(size);
        item.setFlavor1(flavor1);
        item.setFlavor2(flavor2);
        item.setQuantity(itemDTO.getQuantity());
        item.setNotes(itemDTO.getNotes());
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

        updateOnDemandStatus(item);

        if (item.getOnDemand()) {
            item.setStatus(OrderStatus.PENDING);
        } else {
            item.setStatus(OrderStatus.READY_FOR_PICKUP);
            orderService.adjustRemainingStock(flavor1, size, -item.getQuantity());
        }
        return item;
    }

    private void updateOnDemandStatus(OrderItem item) {
        boolean onDemand = (item.getFlavor2() != null) ||
                "1Kg".equalsIgnoreCase(item.getSize().getName()) ||
                (item.getNotes() != null && !item.getNotes().isBlank());
        item.setOnDemand(onDemand);
    }

    private void updateStockForModification(boolean wasOnDemand, boolean isNowOnDemand, int originalQuantity, Flavor originalFlavor1, Size originalSize, OrderItem currentItem) {
        if (!wasOnDemand && isNowOnDemand) {
            orderService.adjustRemainingStock(originalFlavor1, originalSize, originalQuantity);
        } else if (wasOnDemand && !isNowOnDemand) {
            orderService.adjustRemainingStock(currentItem.getFlavor1(), currentItem.getSize(), -currentItem.getQuantity());
        } else if (!wasOnDemand && !isNowOnDemand) {
            boolean hasChanged = !Objects.equals(originalFlavor1.getId(), currentItem.getFlavor1().getId()) ||
                    !Objects.equals(originalSize.getId(), currentItem.getSize().getId());

            if (hasChanged) {
                orderService.adjustRemainingStock(originalFlavor1, originalSize, originalQuantity);
                orderService.adjustRemainingStock(currentItem.getFlavor1(), currentItem.getSize(), -currentItem.getQuantity());
            } else {
                int quantityDifference = originalQuantity - currentItem.getQuantity();
                if (quantityDifference != 0) {
                    orderService.adjustRemainingStock(currentItem.getFlavor1(), currentItem.getSize(), quantityDifference);
                }
            }
        }
    }

    private boolean applyPatch(OrderItem item, OrderItemPatchRequestDTO patchDTO) {
        boolean priceChanged = false;
        if (patchDTO.getQuantity() != null && patchDTO.getQuantity() > 0) {
            item.setQuantity(patchDTO.getQuantity());
        }
        if (patchDTO.getSizeId() != null && !Objects.equals(item.getSize().getId(), patchDTO.getSizeId())) {
            item.setSize(findSizeOrFail(patchDTO.getSizeId()));
            priceChanged = true;
        }
        if (patchDTO.getFlavor1Id() != null && !Objects.equals(item.getFlavor1().getId(), patchDTO.getFlavor1Id())) {
            item.setFlavor1(findFlavorOrFail(patchDTO.getFlavor1Id()));
            priceChanged = true;
        }
        Long currentFlavor2Id = item.getFlavor2() != null ? item.getFlavor2().getId() : null;
        if (!Objects.equals(currentFlavor2Id, patchDTO.getFlavor2Id())) {
            item.setFlavor2(patchDTO.getFlavor2Id() != null ? findFlavorOrFail(patchDTO.getFlavor2Id()) : null);
            priceChanged = true;
        }
        if (patchDTO.getNotes() != null) {
            item.setNotes(patchDTO.getNotes());
        }
        return priceChanged;
    }

    private void recalculatePrices(OrderItem item) {
        BigDecimal newUnitPrice = productPriceService.calculateUnitPrice(item.getSize(), item.getFlavor1(), item.getFlavor2());
        item.setUnitPrice(newUnitPrice);
    }

    private Order findOrderAndValidate(Long orderId, Long customerId) {
        return orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId + " and customer id: " + customerId));
    }

    private OrderItem findOrderItemOrFail(Long itemId, Long orderId) {
        return orderItemRepository.findByIdAndOrder_Id(itemId, orderId)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found with id: " + itemId + " for order id: " + orderId));
    }

    private Size findSizeOrFail(Long sizeId) {
        return sizeRepository.findById(sizeId)
                .orElseThrow(() -> new EntityNotFoundException("Size not found with id: " + sizeId));
    }

    private Flavor findFlavorOrFail(Long flavorId) {
        return flavorRepository.findById(flavorId)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found with id: " + flavorId));
    }
}