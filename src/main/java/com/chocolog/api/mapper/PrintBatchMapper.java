package com.chocolog.api.mapper;

import com.chocolog.api.dto.response.PrintBatchDetailResponseDTO;
import com.chocolog.api.dto.response.PrintBatchListResponseDTO;
import com.chocolog.api.model.PrintBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class, PrintBatchItemMapper.class})
public interface PrintBatchMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "fileSystemPath", source = "fileSystemPath")
    @Mapping(target = "printedBy", source = "printedBy")
    @Mapping(target = "items", source = "items")
    PrintBatchDetailResponseDTO toDetailResponseDTO(PrintBatch printBatch);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "printedBy", source = "printedBy")
    @Mapping(target = "itemsCount", expression = "java(printBatch.getItems() != null ? printBatch.getItems().size() : 0)")
    PrintBatchListResponseDTO toListResponseDTO(PrintBatch printBatch);
}