package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.dto.response.PrintBatchItemResponseDTO;
import com.chocolog.api.model.OrderItem;
import com.chocolog.api.model.PrintBatchItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrintBatchItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderItem", source = "orderItem")
    PrintBatchItemResponseDTO toResponseDTO(PrintBatchItem printBatchItem);


    @Mapping(target = "id", source = "orderItem.id")
    @Mapping(target = "orderId", source = "orderItem.order.id")
    @Mapping(target = "sizeId", source = "orderItem.size.id")
    @Mapping(target = "sizeName", source = "orderItem.size.name")
    @Mapping(target = "flavor1Id", source = "orderItem.flavor1.id")
    @Mapping(target = "flavor1Name", source = "orderItem.flavor1.name")
    @Mapping(target = "flavor2Id", source = "orderItem.flavor2.id")
    @Mapping(target = "flavor2Name", source = "orderItem.flavor2.name")
    @Mapping(target = "quantity", source = "orderItem.quantity")
    @Mapping(target = "unitPrice", source = "orderItem.unitPrice")
    @Mapping(target = "totalPrice", source = "orderItem.totalPrice")
    @Mapping(target = "onDemand", source = "orderItem.onDemand")
    @Mapping(target = "notes", source = "orderItem.notes")
    @Mapping(target = "status", expression = "java(orderItem.getStatus() != null ? orderItem.getStatus().toString() : null)")
    @Mapping(target = "customerId", source = "orderItem.order.customer.id")
    @Mapping(target = "customerName", source = "orderItem.order.customer.name")
    @Mapping(target = "customerPhone", source = "orderItem.order.customer.phone")
    @Mapping(target = "expectedPickupDate", source = "orderItem.order.expectedPickupDate")
    @Mapping(target = "isPrinted", constant = "true")
    OrderItemResponseDTO orderItemToOrderItemResponseDTO(OrderItem orderItem);
}