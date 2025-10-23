package com.chocolog.api.dto.response.reports;

import com.chocolog.api.model.OrderStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersByStatusDTO {

    private OrderStatus status;
    private Long count;

}