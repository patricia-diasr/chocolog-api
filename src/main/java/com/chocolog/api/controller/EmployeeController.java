package com.chocolog.api.controller;

import com.chocolog.api.dto.request.EmployeePatchRequestDTO;
import com.chocolog.api.dto.request.EmployeeRequestDTO;
import com.chocolog.api.dto.response.EmployeeResponseDTO;
import com.chocolog.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/employees")
@Tag(name = "Funcionários", description = "API para gerenciamento de funcionários")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Listar todos os funcionários", description = "Retorna uma lista com todos os funcionários cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de funcionários retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @Operation(summary = "Buscar funcionário por ID", description = "Retorna um funcionário específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "ID do funcionário") @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @Operation(summary = "Criar novo funcionário", description = "Cria um novo funcionário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO employeeDTO) {
        EmployeeResponseDTO createdEmployee = employeeService.save(employeeDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdEmployee.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdEmployee);
    }

    @Operation(summary = "Atualizar parcialmente um funcionário", description = "Atualiza campos específicos de um funcionário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> partialUpdateEmployee(
            @Parameter(description = "ID do funcionário") @PathVariable Long id,
            @Valid @RequestBody EmployeePatchRequestDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.update(id, employeeDTO));
    }

    @Operation(summary = "Deletar funcionário", description = "Remove um funcionário do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@Parameter(description = "ID do funcionário") @PathVariable Long id) {
        employeeService.deleteById(id);
    }
}
