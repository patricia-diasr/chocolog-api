package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotalByFlavorAndSizeDTO {

    private String flavor;
    private List<SizeSalesDTO> sizeSales;

}