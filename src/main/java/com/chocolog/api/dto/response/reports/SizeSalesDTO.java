package com.chocolog.api.dto.response.reports;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SizeSalesDTO {

    private String size;
    private Integer quantity;

}
