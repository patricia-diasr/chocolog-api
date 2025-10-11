package com.chocolog.api.mapper;

import com.chocolog.api.dto.request.CustomerRequestDTO;
import com.chocolog.api.dto.response.CustomerResponseDTO;
import com.chocolog.api.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponseDTO toResponseDTO(Customer customer);
    Customer toEntity(CustomerRequestDTO requestDTO);

    void updateEntityFromDto(CustomerRequestDTO dto, @MappingTarget Customer entity);
}