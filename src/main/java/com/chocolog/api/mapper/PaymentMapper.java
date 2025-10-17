package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.PaymentResponseDTO;
import com.chocolog.api.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "charge.id", target = "chargeId")
    @Mapping(source = "employee.id", target = "employeeId")
    PaymentResponseDTO toResponseDTO(Payment payment);

}