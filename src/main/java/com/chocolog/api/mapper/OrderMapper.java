package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, ChargeMapper.class})
public interface OrderMapper {

    OrderResponseDTO toResponseDTO(Order order);

}