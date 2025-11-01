package com.chocolog.api.service;

import com.chocolog.api.dto.request.OrderItemRequestDTO;
import com.chocolog.api.dto.request.OrderPatchRequestDTO;
import com.chocolog.api.dto.request.OrderRequestDTO;
import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.mapper.OrderMapper;
import com.chocolog.api.model.*;
import com.chocolog.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ChargeRepository chargeRepository;
    private final SizeRepository sizeRepository;
    private final FlavorRepository flavorRepository;
    private final ProductPriceService productPriceService;
    private final StockRepository stockRepository;
    private final OrderMapper orderMapper;

    public List<OrderResponseDTO> findAllByDateFilter(String dateString) {
        LocalDate startDate;
        LocalDate endDate;

        if (dateString.length() == 7) {
            YearMonth yearMonth = YearMonth.parse(dateString);
            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        } else if (dateString.length() == 10) {
            startDate = LocalDate.parse(dateString);
            endDate = startDate;
        } else {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM or YYYY-MM-DD.");
        }

        List<Order> orders = orderRepository.findByExpectedPickupDateBetweenOrderByExpectedPickupDate(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59, 999999999)
        );

        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> findAllByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found for id: " + customerId);
        }
        return orderRepository.findByCustomerId(customerId).stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    public OrderResponseDTO findByIdAndCustomerId(Long orderId, Long customerId) {
        Order order = findOrderOrFail(orderId, customerId);
        return orderMapper.toResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO create(Long customerId, OrderRequestDTO orderDTO, Long employeeId) {
        Customer customer = findCustomerOrFail(customerId);
        Employee employee = findEmployeeOrFail(employeeId);

        Order order = new Order();
        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setExpectedPickupDate(orderDTO.getExpectedPickupDate());
        order.setNotes(orderDTO.getNotes());
        order.setCreationDate(LocalDateTime.now());

        List<OrderItem> items = orderDTO.getOrderItems().stream()
                .map(itemDTO -> buildOrderItem(itemDTO, order))
                .collect(Collectors.toList());
        order.setOrderItems(items);

        boolean hasPendingItems = items.stream().anyMatch(item -> item.getStatus() == OrderStatus.PENDING);
        order.setStatus(hasPendingItems ? OrderStatus.PENDING : OrderStatus.READY_FOR_PICKUP);

        Charge charge = createChargeForOrder(order, orderDTO.getDiscount());
        order.setCharges(charge);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponseDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO update(Long orderId, Long customerId, OrderPatchRequestDTO orderDTO) {
        Order existingOrder = findOrderOrFail(orderId, customerId);

        if (existingOrder.getStatus() == OrderStatus.COMPLETED || existingOrder.getStatus() == OrderStatus.CANCELLED) {
            if (orderDTO.getStatus() != null || orderDTO.getExpectedPickupDate() != null) {
                throw new IllegalStateException("Only 'notes' and 'discount' can be updated for a COMPLETED or CANCELLED order.");
            }
        }

        if (orderDTO.getStatus() != null) {
            OrderStatus newStatus = OrderStatus.valueOf(orderDTO.getStatus());

            if (newStatus == OrderStatus.COMPLETED) {
                boolean allItemsReady = existingOrder.getOrderItems().stream()
                        .allMatch(item -> item.getStatus() == OrderStatus.READY_FOR_PICKUP || item.getStatus() == OrderStatus.COMPLETED);

                if (!allItemsReady) {
                    throw new IllegalStateException("Order can only be marked as COMPLETED if all items are READY_FOR_PICKUP or already COMPLETED.");
                }

                existingOrder.setStatus(OrderStatus.COMPLETED);
                completeOrderItemsAndAdjustStock(existingOrder);

            } else if (newStatus == OrderStatus.CANCELLED) {
                boolean hasCompletedItems = existingOrder.getOrderItems().stream()
                        .anyMatch(item -> item.getStatus() == OrderStatus.COMPLETED);

                if (hasCompletedItems) {
                    throw new IllegalStateException("Cannot cancel an order that has completed items.");
                }

                existingOrder.setStatus(OrderStatus.CANCELLED);
                cancelOrderItemsAndReturnStock(existingOrder);
            } else {
                throw new IllegalArgumentException("Order status can only be manually updated to COMPLETED or CANCELLED.");
            }
        }

        if (orderDTO.getExpectedPickupDate() != null) {
            existingOrder.setExpectedPickupDate(orderDTO.getExpectedPickupDate());
        }

        if (orderDTO.getNotes() != null) {
            existingOrder.setNotes(orderDTO.getNotes());
        }

        if (orderDTO.getDiscount() != null) {
            Charge charge = existingOrder.getCharges();
            if (charge == null) {
                throw new IllegalStateException("Order does not have an associated charge to update.");
            }
            charge.setDiscount(orderDTO.getDiscount());
            BigDecimal totalAmount = charge.getSubtotalAmount().subtract(orderDTO.getDiscount());
            charge.setTotalAmount(totalAmount);
            chargeRepository.save(charge);
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        return orderMapper.toResponseDTO(updatedOrder);
    }

    @Transactional
    public void recalculateAndSaveOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        List<OrderItem> items = order.getOrderItems();

        if (items.stream().allMatch(item -> item.getStatus() == OrderStatus.COMPLETED)) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (items.stream().noneMatch(item -> item.getStatus() == OrderStatus.PENDING)) {
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
        } else {
            order.setStatus(OrderStatus.PENDING);
        }

        orderRepository.save(order);
    }

    @Transactional
    void recalculateOrderCharge(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId));

        BigDecimal subtotalAmount = order.getOrderItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Charge charge = order.getCharges();
        if (charge == null) {
            throw new IllegalStateException("Order does not have an associated charge to update.");
        }

        charge.setSubtotalAmount(subtotalAmount);
        BigDecimal totalAmount = subtotalAmount.subtract(charge.getDiscount());
        charge.setTotalAmount(totalAmount);

        chargeRepository.save(charge);
    }

    public void adjustRemainingStock(Flavor flavor, Size size, int quantityDelta) {
        if (quantityDelta == 0) return;

        Stock stock = stockRepository.findByFlavorAndSize(flavor, size)
                .orElseThrow(() -> new IllegalStateException("Stock not found for flavor id: " + flavor.getId() + " and size id: " + size.getId()));

        stock.setRemainingQuantity(stock.getRemainingQuantity() + quantityDelta);
        stockRepository.save(stock);
    }

    public void adjustTotalStock(Flavor flavor, Size size, int quantityDelta) {
        if (quantityDelta == 0) return;
        Stock stock = stockRepository.findByFlavorAndSize(flavor, size)
                .orElseThrow(() -> new IllegalStateException("Stock not found..."));
        stock.setTotalQuantity(stock.getTotalQuantity() + quantityDelta);
        stockRepository.save(stock);
    }

    private Charge createChargeForOrder(Order order, BigDecimal discount) {
        BigDecimal discountValue = discount != null ? discount : BigDecimal.ZERO;

        BigDecimal subtotalAmount = order.getOrderItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = subtotalAmount.subtract(discountValue);

        Charge charge = new Charge();
        charge.setDiscount(discountValue);
        charge.setSubtotalAmount(subtotalAmount);
        charge.setTotalAmount(totalAmount);
        charge.setStatus(ChargeStatus.UNPAID);
        charge.setOrder(order);

        return charge;
    }

    private OrderItem buildOrderItem(OrderItemRequestDTO itemDTO, Order order) {
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

        boolean onDemand = (flavor2 != null) || "1Kg".equalsIgnoreCase(size.getName()) || (itemDTO.getNotes() != null && !itemDTO.getNotes().isBlank());
        item.setOnDemand(onDemand);

        if (onDemand) {
            item.setStatus(OrderStatus.PENDING);
        } else {
            item.setStatus(OrderStatus.READY_FOR_PICKUP);
            adjustRemainingStock(flavor1, size, -itemDTO.getQuantity());
        }

        return item;
    }

    private void completeOrderItemsAndAdjustStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            if (!item.getOnDemand() && item.getStatus() != OrderStatus.COMPLETED) {
                adjustTotalStock(item.getFlavor1(), item.getSize(), -item.getQuantity());
            }
            item.setStatus(OrderStatus.COMPLETED);
        }
    }

    private void cancelOrderItemsAndReturnStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            if (!item.getOnDemand() && item.getStatus() != OrderStatus.CANCELLED) {
                adjustRemainingStock(item.getFlavor1(), item.getSize(), item.getQuantity());
            }
            item.setStatus(OrderStatus.CANCELLED);
        }
    }

    private Size findSizeOrFail(Long sizeId) {
        return sizeRepository.findById(sizeId)
                .orElseThrow(() -> new EntityNotFoundException("Size not found with id: " + sizeId));
    }

    private Flavor findFlavorOrFail(Long flavorId) {
        return flavorRepository.findById(flavorId)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found with id: " + flavorId));
    }

    private Order findOrderOrFail(Long orderId, Long customerId) {
        return orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId + " and customer id: " + customerId));
    }

    private Customer findCustomerOrFail(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found for id: " + customerId));
    }

    private Employee findEmployeeOrFail(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + employeeId));
    }
}