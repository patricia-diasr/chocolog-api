package com.chocolog.api.mapper;

import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.model.Flavor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;

@Mapper(componentModel = "spring")
public interface FlavorMapper {
    
    FlavorResponseDTO toResponseDTO(Flavor flavor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "flavor1Orders", ignore = true)
    @Mapping(target = "flavor2Orders", ignore = true)
    @Mapping(target = "productPrices", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "stockRecords", ignore = true)
    Flavor toEntity(FlavorRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "flavor1Orders", ignore = true)
    @Mapping(target = "flavor2Orders", ignore = true)
    @Mapping(target = "productPrices", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "stockRecords", ignore = true)
    void updateEntityFromDto(FlavorRequestDTO dto, @MappingTarget Flavor entity);
}