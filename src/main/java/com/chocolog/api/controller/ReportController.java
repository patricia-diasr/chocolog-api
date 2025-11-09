package com.chocolog.api.controller;

import com.chocolog.api.dto.response.reports.ReportsDTO;
import com.chocolog.api.model.PeriodType;
import com.chocolog.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "Relatórios", description = "API para geração de relatórios e dashboards")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Obter relatórios do dashboard", description = "Retorna dados consolidados para exibição no dashboard com base em período especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios gerados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportsDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ReportsDTO> getDashboardReports(
            @Parameter(description = "Data inicial no formato yyyy-MM-dd")
            @RequestParam(name = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Data final no formato yyyy-MM-dd")
            @RequestParam(name = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Tipo de período para agrupamento (DAY, WEEK, MONTH, YEAR)")
            @RequestParam(name = "periodType", defaultValue = "WEEK") PeriodType periodType
    ) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return ResponseEntity.ok(reportService.getDashboardReports(startDateTime, endDateTime, periodType));
    }
}
