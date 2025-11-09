package com.chocolog.api.controller;

import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.request.FlavorPatchRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.service.FlavorService;
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
@RequestMapping("/flavors")
@Tag(name = "Sabores", description = "API para gerenciamento de sabores")
public class FlavorController {

    private final FlavorService flavorService;

    @Operation(summary = "Listar todos os sabores", description = "Retorna uma lista com todos os sabores cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de sabores retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlavorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<FlavorResponseDTO>> getAllFlavors() {
        return ResponseEntity.ok(flavorService.findAll());
    }

    @Operation(summary = "Buscar sabor por ID", description = "Retorna um sabor específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sabor encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlavorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sabor não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlavorResponseDTO> getFlavorById(
            @Parameter(description = "ID do sabor") @PathVariable Long id) {
        return ResponseEntity.ok(flavorService.findById(id));
    }

    @Operation(summary = "Criar novo sabor", description = "Cria um novo sabor no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sabor criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlavorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FlavorResponseDTO> createFlavor(
            @Valid @RequestBody FlavorRequestDTO flavorDTO) {
        FlavorResponseDTO createdFlavor = flavorService.save(flavorDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdFlavor.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdFlavor);
    }

    @Operation(summary = "Atualizar parcialmente um sabor", description = "Atualiza campos específicos de um sabor existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sabor atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlavorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sabor não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<FlavorResponseDTO> partialUpdateFlavor(
            @Parameter(description = "ID do sabor") @PathVariable Long id,
            @Valid @RequestBody FlavorPatchRequestDTO flavorDTO) {
        return ResponseEntity.ok(flavorService.update(id, flavorDTO));
    }

    @Operation(summary = "Deletar sabor", description = "Remove um sabor do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sabor deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sabor não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlavor(@Parameter(description = "ID do sabor") @PathVariable Long id) {
        flavorService.deleteById(id);
    }
}
