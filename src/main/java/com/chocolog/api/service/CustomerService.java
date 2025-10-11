package com.chocolog.api.service;

import com.chocolog.api.dto.request.CustomerPatchRequestDTO;
import com.chocolog.api.dto.request.CustomerRequestDTO;
import com.chocolog.api.dto.response.CustomerResponseDTO;
import com.chocolog.api.mapper.CustomerMapper;
import com.chocolog.api.model.Customer;
import com.chocolog.api.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper; 

    public List<CustomerResponseDTO> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDTO)
                .toList();
    }

    public CustomerResponseDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found for id: " + id));
        return customerMapper.toResponseDTO(customer);
    }

    public CustomerResponseDTO save(CustomerRequestDTO customerDTO) {
        Customer customer = customerMapper.toEntity(customerDTO);

        String digitsOnly = customer.getPhone().replaceAll("[^0-9]", "");
        customer.setPhone(digitsOnly);

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDTO(savedCustomer);
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerPatchRequestDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found for id: " + id));

        if (customerDTO.getName() != null) {
            existingCustomer.setName(customerDTO.getName());
        }
        if (customerDTO.getPhone() != null) {
            String digitsOnly = customerDTO.getPhone().replaceAll("[^0-9]", "");
            existingCustomer.setPhone(digitsOnly);
        }
        if (customerDTO.getIsReseller() != null) {
            existingCustomer.setIsReseller(customerDTO.getIsReseller());
        }
        if (customerDTO.getNotes() != null) {
            existingCustomer.setNotes(customerDTO.getNotes());
        }

        return customerMapper.toResponseDTO(existingCustomer);
    }

    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found for id: " + id));

        customerRepository.delete(customer);
    }
}