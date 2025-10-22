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
    @Mapping(target = "amountDue", source = "charge", qualifiedByName = "calculateAmountDue")
    ChargeResponseDTO toResponseDTO(Charge charge);

    @Named("calculateAmountDue")
    default BigDecimal calculateAmountDue(Charge charge) {
        if (charge == null || charge.getTotalAmount() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = charge.getPayments().stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amountDue = charge.getTotalAmount().subtract(totalPaid);

        return amountDue.compareTo(BigDecimal.ZERO) > 0 ? amountDue : BigDecimal.ZERO;
    }
}