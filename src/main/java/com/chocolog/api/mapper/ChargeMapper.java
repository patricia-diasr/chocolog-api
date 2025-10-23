package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.ChargeResponseDTO;
import com.chocolog.api.model.Charge;
import com.chocolog.api.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {PaymentMapper.class})
public interface ChargeMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.creationDate", target = "date")
    @Mapping(target = "dueAmount", source = "charge", qualifiedByName = "calculateDueAmount")
    ChargeResponseDTO toResponseDTO(Charge charge);

    @Named("calculateDueAmount")
    default BigDecimal calculateDueAmount(Charge charge) {
        if (charge == null || charge.getTotalAmount() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = charge.getPayments().stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dueAmount = charge.getTotalAmount().subtract(totalPaid);

        return dueAmount.compareTo(BigDecimal.ZERO) > 0 ? dueAmount : BigDecimal.ZERO;
    }
}