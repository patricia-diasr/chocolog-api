package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "size.id", target = "sizeId")
    @Mapping(source = "size.name", target = "sizeName")
    @Mapping(source = "flavor1.id", target = "flavor1Id")
    @Mapping(source = "flavor1.name", target = "flavor1Name")
    @Mapping(source = "flavor2.id", target = "flavor2Id")
    @Mapping(source = "flavor2.name", target = "flavor2Name")
    OrderItemResponseDTO toResponseDTO(OrderItem orderItem);

}