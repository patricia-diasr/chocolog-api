package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.OrderResponseDTO;
import com.chocolog.api.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, ChargeMapper.class})
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "employee.id", target = "employeeId")
    OrderResponseDTO toResponseDTO(Order order);

}