package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialsDTO {

    private List<RevenueVsReceivedDTO> revenueVsReceivedByPeriod;
    private List<ReceivedByPaymentMethodDTO> receivedByPaymentMethod;

}