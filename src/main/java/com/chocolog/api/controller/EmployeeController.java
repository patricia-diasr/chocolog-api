package com.chocolog.api.controller;

import com.chocolog.api.dto.request.EmployeePatchRequestDTO;
import com.chocolog.api.dto.request.EmployeeRequestDTO;
import com.chocolog.api.dto.response.EmployeeResponseDTO;
import com.chocolog.api.service.EmployeeService;

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
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeDTO) {
        EmployeeResponseDTO createdEmployee = employeeService.save(employeeDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdEmployee.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdEmployee);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> partialUpdateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeePatchRequestDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.update(id, employeeDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
    }
}