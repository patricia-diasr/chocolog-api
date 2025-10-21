package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.ChargeResponseDTO;
import com.chocolog.api.model.Charge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PaymentMapper.class})
public interface ChargeMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.creationDate", target = "date")
    ChargeResponseDTO toResponseDTO(Charge charge);

}