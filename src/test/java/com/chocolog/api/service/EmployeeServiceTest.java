package com.chocolog.api.service;

import com.chocolog.api.dto.request.EmployeePatchRequestDTO;
import com.chocolog.api.dto.request.EmployeeRequestDTO;
import com.chocolog.api.dto.response.EmployeeResponseDTO;
import com.chocolog.api.mapper.EmployeeMapper;
import com.chocolog.api.model.Employee;
import com.chocolog.api.model.Role;
import com.chocolog.api.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para EmployeeService")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private EmployeeResponseDTO mockResponseDTO;
    private EmployeeRequestDTO mockRequestDTO;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .id(1L)
                .name("Alice Cooper")
                .login("acooper")
                .passwordHash("hashed_password_123")
                .role(Role.ADMIN)
                .build();

        mockResponseDTO = new EmployeeResponseDTO(
                1L,
                "Alice Cooper",
                "acooper",
                Role.ADMIN.name()
        );

        mockRequestDTO = EmployeeRequestDTO.builder()
                .name("Ana Machado")
                .login("ana.machado")
                .password("senha456")
                .role("STAFF")
                .build();
    }

    // --- Testes para findAll ---
    @Test
    @DisplayName("Deve retornar todos os funcionários quando a lista não está vazia")
    void findAll_ShouldReturnEmployees_WhenRepositoryIsNotEmpty() {
        // Arrange
        List<Employee> employees = List.of(mockEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.toResponseDTO(any(Employee.class))).thenReturn(mockResponseDTO);

        // Act
        List<EmployeeResponseDTO> result = employeeService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(employeeMapper, times(1)).toResponseDTO(mockEmployee);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há funcionários")
    void findAll_ShouldReturnEmptyList_WhenRepositoryIsEmpty() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeResponseDTO> result = employeeService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(employeeMapper, never()).toResponseDTO(any());
    }

    // --- Testes para findById ---
    @Test
    @DisplayName("Deve retornar EmployeeResponseDTO quando o ID for encontrado")
    void findById_ShouldReturnEmployeeResponseDTO_WhenIdExists() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toResponseDTO(any(Employee.class))).thenReturn(mockResponseDTO);

        // Act
        EmployeeResponseDTO result = employeeService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockEmployee.getName(), result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.findById(2L));
    }

    // --- Testes para save ---
    @Test
    @DisplayName("Deve salvar novo funcionário e criptografar a senha quando dados válidos são fornecidos")
    void save_ShouldSaveEmployeeAndEncodePassword_WhenValidData() {
        // Arrange
        Employee employeeToSave = Employee.builder()
                .name("Ana Machado")
                .login("ana.machado")
                .role(Role.STAFF)
                .build();
        Employee savedEmployee = Employee.builder()
                .id(2L)
                .name("Ana Machado")
                .login("ana.machado")
                .passwordHash("encoded_senha456")
                .role(Role.STAFF)
                .build();

        when(employeeRepository.findByLogin("ana.machado")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(mockRequestDTO)).thenReturn(employeeToSave);
        when(passwordEncoder.encode("senha456")).thenReturn("encoded_senha456");
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
        when(employeeMapper.toResponseDTO(savedEmployee)).thenReturn(mockResponseDTO);

        // Act
        EmployeeResponseDTO result = employeeService.save(mockRequestDTO);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("senha456");
        verify(employeeRepository, times(1)).save(employeeToSave);
        assertEquals("encoded_senha456", employeeToSave.getPasswordHash());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o login já está em uso ao salvar")
    void save_ShouldThrowException_WhenLoginIsAlreadyInUse() {
        // Arrange
        when(employeeRepository.findByLogin(mockRequestDTO.getLogin())).thenReturn(Optional.of(mockEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.save(mockRequestDTO));
        verify(employeeRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // --- Testes para update ---
    @Test
    @DisplayName("Deve atualizar o nome do funcionário quando fornecido")
    void update_ShouldUpdateEmployeeName_WhenNameIsProvided() {
        // Arrange
        Long employeeId = 1L;
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .name("Alice Cooper Updated")
                .build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toResponseDTO(mockEmployee)).thenReturn(mockResponseDTO);

        // Act
        employeeService.update(employeeId, requestDTO);

        // Assert
        assertEquals("Alice Cooper Updated", mockEmployee.getName());
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).findByLogin(any()); // Não deve checar login
    }

    @Test
    @DisplayName("Deve atualizar o login do funcionário após checagem de unicidade")
    void update_ShouldUpdateEmployeeLogin_WhenNewLoginIsUnique() {
        // Arrange
        Long employeeId = 1L;
        String newLogin = "acooper_new";
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .login(newLogin)
                .build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(employeeRepository.findByLogin(newLogin)).thenReturn(Optional.empty());
        when(employeeMapper.toResponseDTO(mockEmployee)).thenReturn(mockResponseDTO);

        // Act
        employeeService.update(employeeId, requestDTO);

        // Assert
        assertEquals(newLogin, mockEmployee.getLogin());
        verify(employeeRepository, times(1)).findByLogin(newLogin);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o novo login já está em uso no update")
    void update_ShouldThrowException_WhenNewLoginIsAlreadyInUse() {
        // Arrange
        Long employeeId = 1L;
        String conflictingLogin = "existing_user";
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .login(conflictingLogin)
                .build();
        Employee conflictingEmployee = Employee.builder().id(99L).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(employeeRepository.findByLogin(conflictingLogin)).thenReturn(Optional.of(conflictingEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(employeeId, requestDTO));
        verify(employeeRepository, times(1)).findByLogin(conflictingLogin);
    }

    @Test
    @DisplayName("Não deve checar unicidade nem atualizar login se for o mesmo valor atual (case-insensitive)")
    void update_ShouldNotUpdateLogin_WhenLoginIsTheSame() {
        // Arrange
        Long employeeId = 1L;
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .login("aCOOPER")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toResponseDTO(mockEmployee)).thenReturn(mockResponseDTO);

        // Act
        employeeService.update(employeeId, requestDTO);

        // Assert
        assertEquals("acooper", mockEmployee.getLogin());
        verify(employeeRepository, never()).findByLogin(any());
    }

    @Test
    @DisplayName("Deve atualizar a Role do funcionário")
    void update_ShouldUpdateEmployeeRole_WhenRoleIsProvided() {
        // Arrange
        Long employeeId = 1L;
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .role("STAFF")
                .build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toResponseDTO(mockEmployee)).thenReturn(mockResponseDTO);

        // Act
        employeeService.update(employeeId, requestDTO);

        // Assert
        assertEquals(Role.STAFF, mockEmployee.getRole());
    }

    @Test
    @DisplayName("Deve criptografar e atualizar a senha quando fornecida")
    void update_ShouldUpdatePasswordWithEncodedValue_WhenPasswordIsProvided() {
        // Arrange
        Long employeeId = 1L;
        String newPassword = "new_secret_password";
        String encodedPassword = "new_encoded_hash";
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .password(newPassword)
                .build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(employeeMapper.toResponseDTO(mockEmployee)).thenReturn(mockResponseDTO);

        // Act
        employeeService.update(employeeId, requestDTO);

        // Assert
        assertEquals(encodedPassword, mockEmployee.getPasswordHash());
        verify(passwordEncoder, times(1)).encode(newPassword);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado no update")
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long employeeId = 99L;
        EmployeePatchRequestDTO requestDTO = EmployeePatchRequestDTO.builder()
                .name("Teste")
                .build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> employeeService.update(employeeId, requestDTO));
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).findByLogin(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // --- Testes para deleteById ---
    @Test
    @DisplayName("Deve deletar o funcionário quando o ID é encontrado")
    void deleteById_ShouldDeleteEmployee_WhenIdExists() {
        // Arrange
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));

        // Act
        employeeService.deleteById(employeeId);

        // Assert
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).delete(mockEmployee);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado no delete")
    void deleteById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long employeeId = 99L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> employeeService.deleteById(employeeId));
        verify(employeeRepository, never()).delete(any());
    }
}