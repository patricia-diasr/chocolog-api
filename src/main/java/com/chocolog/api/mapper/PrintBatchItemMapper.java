package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.PrintBatchItemResponseDTO;
import com.chocolog.api.model.PrintBatchItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface PrintBatchItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderItem", source = "orderItem")
    PrintBatchItemResponseDTO toResponseDTO(PrintBatchItem printBatchItem);

}