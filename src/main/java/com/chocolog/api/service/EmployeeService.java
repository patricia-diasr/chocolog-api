package com.chocolog.api.service;

import com.chocolog.api.dto.request.EmployeePatchRequestDTO;
import com.chocolog.api.dto.request.EmployeeRequestDTO;
import com.chocolog.api.dto.response.EmployeeResponseDTO;
import com.chocolog.api.mapper.EmployeeMapper;
import com.chocolog.api.model.Employee;
import com.chocolog.api.model.Role;
import com.chocolog.api.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder; 

    public List<EmployeeResponseDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponseDTO)
                .toList();
    }

    public EmployeeResponseDTO findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + id));
        return employeeMapper.toResponseDTO(employee);
    }

    public EmployeeResponseDTO save(EmployeeRequestDTO employeeDTO) {
        employeeRepository.findByLogin(employeeDTO.getLogin()).ifPresent(e -> {
            throw new IllegalArgumentException("The login '" + employeeDTO.getLogin() + "' is already in use.");
        });

        Employee employee = employeeMapper.toEntity(employeeDTO);

        employee.setPasswordHash(passwordEncoder.encode(employeeDTO.getPassword()));

        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponseDTO(savedEmployee);
    }

    @Transactional
    public EmployeeResponseDTO update(Long id, EmployeePatchRequestDTO employeeDTO) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + id));

        if (employeeDTO.getName() != null) {
            existingEmployee.setName(employeeDTO.getName());
        }
        if (employeeDTO.getLogin() != null && !employeeDTO.getLogin().equalsIgnoreCase(existingEmployee.getLogin())) {
            employeeRepository.findByLogin(employeeDTO.getLogin()).ifPresent(e -> {
                throw new IllegalArgumentException("The login '" + employeeDTO.getLogin() + "' is already in use.");
            });
            existingEmployee.setLogin(employeeDTO.getLogin());
        }
        if (employeeDTO.getRole() != null) {
            Role role = Role.valueOf(employeeDTO.getRole().toUpperCase());
            existingEmployee.setRole(role);
        }
        if (employeeDTO.getPassword() != null) {
            existingEmployee.setPasswordHash(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        return employeeMapper.toResponseDTO(existingEmployee);
    }

    public void deleteById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + id));

        employeeRepository.delete(employee);
    }
}