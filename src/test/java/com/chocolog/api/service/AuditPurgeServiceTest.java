package com.chocolog.api.service;

import com.chocolog.api.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para AuditPurgeService")
public class AuditPurgeServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditPurgeService auditPurgeService;

    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 11, 15, 10, 0, 0);
    private final int retentionDays = 45;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auditPurgeService, "retentionDays", retentionDays);
    }

    @Test
    @DisplayName("Deve chamar o delete com a data de corte correta")
    void purgeOldAudits_ShouldCallDeleteWithCorrectCutoffDate() {
        // Arrange
        LocalDateTime expectedCutoff = fixedNow.minusDays(retentionDays);

        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedNow);
            when(auditRepository.deleteByCreatedAtBefore(expectedCutoff)).thenReturn(5L);

            // Act
            auditPurgeService.purgeOldAudits();

            // Assert
            verify(auditRepository, times(1)).deleteByCreatedAtBefore(expectedCutoff);
        }
    }

    @Test
    @DisplayName("Deve capturar e logar exceção do repositório (e não quebrar)")
    void purgeOldAudits_ShouldCatchAndLogRepositoryException() {
        // Arrange
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(auditRepository.deleteByCreatedAtBefore(captor.capture()))
                .thenThrow(new DataIntegrityViolationException("Erro de DB simulado"));

        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedNow);

            // --- Act & Assert
            assertDoesNotThrow(() -> auditPurgeService.purgeOldAudits(),
                    "O serviço não deve propagar a exceção do repositório");
            verify(auditRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
            assertEquals(fixedNow.minusDays(retentionDays), captor.getValue());
        }
    }
}