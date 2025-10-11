package com.chocolog.api.mapper;

import com.chocolog.api.dto.request.CustomerRequestDTO;
import com.chocolog.api.dto.response.CustomerResponseDTO;
import com.chocolog.api.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponseDTO toResponseDTO(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CustomerRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    void updateEntityFromDto(CustomerRequestDTO dto, @MappingTarget Customer entity);
}