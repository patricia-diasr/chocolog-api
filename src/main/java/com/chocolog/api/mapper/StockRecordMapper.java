package com.chocolog.api.mapper;

import com.chocolog.api.model.StockRecord;
import com.chocolog.api.dto.request.StockRecordRequestDTO;
import com.chocolog.api.dto.response.StockRecordResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockRecordMapper {
    @Mapping(source = "flavor.name", target = "flavorName")
    @Mapping(source = "size.name", target = "sizeName")
    StockRecordResponseDTO toResponseDTO(StockRecord stockRecord);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "flavor", ignore = true) 
    @Mapping(target = "size", ignore = true)   
    @Mapping(target = "productionDate", ignore = true)   
    @Mapping(target = "expirationDate", ignore = true)   
    StockRecord toEntity(StockRecordRequestDTO requestDTO);
}