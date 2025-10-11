package com.chocolog.api.mapper;

import com.chocolog.api.dto.request.EmployeePatchRequestDTO;
import com.chocolog.api.dto.request.EmployeeRequestDTO;
import com.chocolog.api.dto.response.EmployeeResponseDTO;
import com.chocolog.api.model.Employee;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    
    EmployeeResponseDTO toResponseDTO(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "printBatches", ignore = true)
    @Mapping(target = "audits", ignore = true)
    Employee toEntity(EmployeeRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "printBatches", ignore = true)
    @Mapping(target = "audits", ignore = true)
    void updateEntityFromDto(EmployeePatchRequestDTO dto, @MappingTarget Employee entity);
}