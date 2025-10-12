package com.chocolog.api.service;

import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.mapper.FlavorMapper;
import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.Role;
import com.chocolog.api.repository.FlavorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FlavorService {

    private final FlavorRepository flavorRepository;
    private final FlavorMapper flavorMapper;

    public List<FlavorResponseDTO> findAll() {
        return flavorRepository.findAll().stream()
                .map(flavorMapper::toResponseDTO)
                .toList();
    }

    public FlavorResponseDTO findById(Long id) {
        Flavor flavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));
        return flavorMapper.toResponseDTO(flavor);
    }

    public FlavorResponseDTO save(FlavorRequestDTO flavorDTO) {
        flavorRepository.findByName(flavorDTO.getName()).ifPresent(e -> {
            throw new IllegalArgumentException("The name '" + flavorDTO.getName() + "' is already in use.");
        });

        Flavor flavor = flavorMapper.toEntity(flavorDTO);

        Flavor savedFlavor = flavorRepository.save(flavor);
        return flavorMapper.toResponseDTO(savedFlavor);
    }

    @Transactional
    public FlavorResponseDTO update(Long id, FlavorRequestDTO flavorDTO) {
        Flavor existingFlavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));


        if (flavorDTO.getName() != null && !flavorDTO.getName().equalsIgnoreCase(existingFlavor.getName())) {
            flavorRepository.findByName(flavorDTO.getName()).ifPresent(e -> {
                throw new IllegalArgumentException("The name '" + flavorDTO.getName() + "' is already in use.");
            });
            existingFlavor.setName(flavorDTO.getName());
        }

        return flavorMapper.toResponseDTO(existingFlavor);
    }

    public void deleteById(Long id) {
        Flavor flavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));

        flavorRepository.delete(flavor);
    }
}