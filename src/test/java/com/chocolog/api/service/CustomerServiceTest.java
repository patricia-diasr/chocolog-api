package com.chocolog.api.service;

import com.chocolog.api.dto.request.CustomerPatchRequestDTO;
import com.chocolog.api.dto.request.CustomerRequestDTO;
import com.chocolog.api.dto.response.CustomerResponseDTO;
import com.chocolog.api.mapper.CustomerMapper;
import com.chocolog.api.model.Customer;
import com.chocolog.api.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para CustomerService")
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer mockCustomer;
    private CustomerResponseDTO mockResponseDTO;

    @BeforeEach
    void setUp() {
        mockCustomer = Customer.builder()
                .id(1L)
                .name("João Silva")
                .phone("11987654321")
                .isReseller(false)
                .notes("Informação adicional")
                .build();

        mockResponseDTO = new CustomerResponseDTO(
                1L,
                "João Silva",
                "11987654321",
                false,
                "Informação adicional"
        );
    }

    // --- Testes para findAll ---
    @Test
    @DisplayName("Deve retornar todos os clientes quando a lista não está vazia")
    void findAll_ShouldReturnCustomers_WhenRepositoryIsNotEmpty() {
        // Arrange
        List<Customer> customers = List.of(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<CustomerResponseDTO> result = customerService.findAll();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(customerMapper, times(1)).toResponseDTO(mockCustomer);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes")
    void findAll_ShouldReturnEmptyList_WhenRepositoryIsEmpty() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CustomerResponseDTO> result = customerService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(customerMapper, never()).toResponseDTO(any());
    }

    // --- Testes para findById ---
    @Test
    @DisplayName("Deve retornar CustomerResponseDTO quando o ID for encontrado")
    void findById_ShouldReturnCustomerResponseDTO_WhenIdExists() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerMapper.toResponseDTO(any(Customer.class))).thenReturn(mockResponseDTO);

        // Act
        CustomerResponseDTO result = customerService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockCustomer.getName(), result.getName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> customerService.findById(2L));
    }

    // --- Testes para save ---
    @Test
    @DisplayName("Deve salvar novo cliente com telefone formatado quando dados válidos são fornecidos")
    void save_ShouldSaveCustomerWithFormattedPhone_WhenValidData() {
        // Arrange
        CustomerRequestDTO requestDTO = CustomerRequestDTO.builder()
                .name("Maria Santos")
                .phone("(11) 98765-4321")
                .isReseller(true)
                .notes("Informações adicionais")
                .build();

        Customer customerToSave = Customer.builder()
                .name("Maria Santos")
                .phone("(11) 98765-4321")
                .isReseller(true)
                .notes("Revendedora")
                .build();

        when(customerMapper.toEntity(requestDTO)).thenReturn(customerToSave);
        when(customerRepository.save(customerToSave)).thenReturn(customerToSave);
        when(customerMapper.toResponseDTO(customerToSave)).thenReturn(mockResponseDTO);

        // Act
        CustomerResponseDTO result = customerService.save(requestDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
        assertEquals("11987654321", customerToSave.getPhone());
    }

    @Test
    @DisplayName("Deve remover caracteres especiais do telefone ao salvar")
    void save_ShouldRemoveSpecialCharactersFromPhone_WhenSaving() {
        // Arrange
        CustomerRequestDTO requestDTO = CustomerRequestDTO.builder()
                .name("Pedro Costa")
                .phone("(11) 91234-5678")
                .isReseller(false)
                .notes(null)
                .build();

        Customer customerToSave = Customer.builder()
                .name("Pedro Costa")
                .phone("(11) 91234-5678")
                .isReseller(false)
                .build();

        when(customerMapper.toEntity(requestDTO)).thenReturn(customerToSave);
        when(customerRepository.save(any(Customer.class))).thenReturn(customerToSave);

        // Act
        customerService.save(requestDTO);

        // Assert
        assertEquals("11912345678", customerToSave.getPhone());
        verify(customerRepository, times(1)).save(customerToSave);
    }

    // --- Testes para update ---
    @Test
    @DisplayName("Deve atualizar o nome do cliente quando fornecido")
    void update_ShouldUpdateCustomerName_WhenNameIsProvided() {
        // Arrange
        Long customerId = 1L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .name("João Silva Atualizado")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertEquals("João Silva Atualizado", mockCustomer.getName());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Deve atualizar o telefone do cliente removendo caracteres especiais")
    void update_ShouldUpdatePhoneWithFormattedValue_WhenPhoneIsProvided() {
        // Arrange
        Long customerId = 1L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .phone("(21) 99999-8888")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertEquals("21999998888", mockCustomer.getPhone());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Deve atualizar o status de revendedor quando fornecido")
    void update_ShouldUpdateIsReseller_WhenIsResellerIsProvided() {
        // Arrange
        Long customerId = 1L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .isReseller(true)
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertTrue(mockCustomer.getIsReseller());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Deve atualizar as notas do cliente quando fornecidas")
    void update_ShouldUpdateNotes_WhenNotesAreProvided() {
        // Arrange
        Long customerId = 1L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .notes("Observações atualizadas")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertEquals("Observações atualizadas", mockCustomer.getNotes());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos quando todos são fornecidos")
    void update_ShouldUpdateAllFields_WhenAllFieldsAreProvided() {
        // Arrange
        Long customerId = 1L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .name("Nome Completo Atualizado")
                .phone("(31) 98888-7777")
                .isReseller(true)
                .notes("Notas completas")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertEquals("Nome Completo Atualizado", mockCustomer.getName());
        assertEquals("31988887777", mockCustomer.getPhone());
        assertTrue(mockCustomer.getIsReseller());
        assertEquals("Notas completas", mockCustomer.getNotes());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não for encontrado no update")
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long customerId = 99L;
        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder()
                .name("Teste")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> customerService.update(customerId, requestDTO));
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Não deve atualizar campos quando valores são nulos")
    void update_ShouldNotUpdateFields_WhenValuesAreNull() {
        // Arrange
        Long customerId = 1L;
        String originalName = mockCustomer.getName();
        String originalPhone = mockCustomer.getPhone();
        Boolean originalIsReseller = mockCustomer.getIsReseller();
        String originalNotes = mockCustomer.getNotes();

        CustomerPatchRequestDTO requestDTO = CustomerPatchRequestDTO.builder().build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.update(customerId, requestDTO);

        // Assert
        assertEquals(originalName, mockCustomer.getName());
        assertEquals(originalPhone, mockCustomer.getPhone());
        assertEquals(originalIsReseller, mockCustomer.getIsReseller());
        assertEquals(originalNotes, mockCustomer.getNotes());
    }
}