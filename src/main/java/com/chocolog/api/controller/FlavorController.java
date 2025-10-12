package com.chocolog.api.controller;

import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.service.FlavorService;

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
@RequestMapping("/flavors")
public class FlavorController {

    private final FlavorService flavorService;

    @GetMapping
    public ResponseEntity<List<FlavorResponseDTO>> getAllFlavors() {
        return ResponseEntity.ok(flavorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlavorResponseDTO> getFlavorById(@PathVariable Long id) {
        return ResponseEntity.ok(flavorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FlavorResponseDTO> createFlavor(@Valid @RequestBody FlavorRequestDTO flavorDTO) {
        FlavorResponseDTO createdFlavor = flavorService.save(flavorDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdFlavor.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdFlavor);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FlavorResponseDTO> partialUpdateFlavor(@PathVariable Long id, @Valid @RequestBody FlavorRequestDTO flavorDTO) {
        return ResponseEntity.ok(flavorService.update(id, flavorDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void deleteFlavor(@PathVariable Long id) {
        flavorService.deleteById(id);
    }
}