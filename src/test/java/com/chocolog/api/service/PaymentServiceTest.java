package com.chocolog.api.service;

import com.chocolog.api.dto.request.PaymentPatchRequestDTO;
import com.chocolog.api.dto.request.PaymentRequestDTO;
import com.chocolog.api.dto.response.PaymentResponseDTO;
import com.chocolog.api.mapper.PaymentMapper;
import com.chocolog.api.model.*;
import com.chocolog.api.repository.ChargeRepository;
import com.chocolog.api.repository.EmployeeRepository;
import com.chocolog.api.repository.OrderRepository;
import com.chocolog.api.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para PaymentService")
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Customer mockCustomer;
    private Order mockOrder;
    private Charge mockCharge;
    private Employee mockEmployee;
    private Payment mockPayment;
    private PaymentRequestDTO mockRequestDTO;
    private PaymentResponseDTO mockResponseDTO;
    private PaymentPatchRequestDTO mockPatchDTO;

    private final Long customerId = 1L;
    private final Long orderId = 1L;
    private final Long employeeId = 1L;
    private final Long paymentId = 1L;
    private final Long chargeId = 1L;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .id(employeeId)
                .name("Funcionário Teste")
                .build();

        mockCustomer = Customer.builder()
                .id(customerId)
                .name("Cliente Teste")
                .build();

        mockOrder = Order.builder()
                .id(orderId)
                .customer(mockCustomer)
                .build();

        mockCharge = Charge.builder()
                .id(chargeId)
                .order(mockOrder)
                .totalAmount(new BigDecimal("100.00"))
                .status(ChargeStatus.UNPAID)
                .payments(new ArrayList<>())
                .build();

        mockPayment = Payment.builder()
                .id(paymentId)
                .charge(mockCharge)
                .employee(mockEmployee)
                .paidAmount(new BigDecimal("50.00"))
                .paymentMethod("PIX")
                .paymentDate(LocalDateTime.now())
                .build();

        mockRequestDTO = PaymentRequestDTO.builder()
                .paidAmount(new BigDecimal("50.00"))
                .paymentMethod("PIX")
                .paymentDate(LocalDateTime.now())
                .build();

        mockPatchDTO = PaymentPatchRequestDTO.builder()
                .paidAmount(new BigDecimal("75.00"))
                .paymentMethod("Credit Card")
                .build();

        mockResponseDTO = new PaymentResponseDTO(
                paymentId,
                chargeId,
                employeeId,
                new BigDecimal("50.00"),
                mockPayment.getPaymentDate(),
                "PIX"
        );
    }

    // --- Métodos Privados Auxiliares ---
    private void mockFindChargeOrFail() {
        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(mockOrder));
        when(chargeRepository.findByOrderId(orderId)).thenReturn(Optional.of(mockCharge));
    }

    private void mockFindEmployeeOrFail() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
    }

    private void mockFindPaymentOrFail() {
        when(paymentRepository.findByIdAndChargeId(paymentId, chargeId)).thenReturn(Optional.of(mockPayment));
    }

    // --- Testes para addPayment ---

    @Test
    @DisplayName("Deve adicionar pagamento e atualizar status para PARCIAL")
    void addPayment_ShouldAddPaymentAndUpdateChargeToPartial_WhenChargeIsUnpaid() {
        // Arrange
        mockFindChargeOrFail();
        mockFindEmployeeOrFail();
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(paymentMapper.toResponseDTO(mockPayment)).thenReturn(mockResponseDTO);

        // Act
        PaymentResponseDTO result = paymentService.addPayment(customerId, orderId, employeeId, mockRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponseDTO.getId(), result.getId());
        assertEquals(ChargeStatus.PARTIAL, mockCharge.getStatus());
        assertTrue(mockCharge.getPayments().contains(mockPayment));
        verify(chargeRepository, times(1)).save(mockCharge);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Deve adicionar pagamento e atualizar status para PAGO")
    void addPayment_ShouldAddPaymentAndUpdateChargeToPaid_WhenPaymentCoversTotal() {
        // Arrange
        mockFindChargeOrFail();
        mockFindEmployeeOrFail();

        PaymentRequestDTO fullPaymentDTO = PaymentRequestDTO.builder()
                .paidAmount(new BigDecimal("100.00"))
                .paymentMethod("PIX")
                .paymentDate(LocalDateTime.now())
                .build();

        mockPayment.setPaidAmount(new BigDecimal("100.00"));

        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(paymentMapper.toResponseDTO(mockPayment)).thenReturn(mockResponseDTO);

        // Act
        PaymentResponseDTO result = paymentService.addPayment(customerId, orderId, employeeId, fullPaymentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(ChargeStatus.PAID, mockCharge.getStatus());
        verify(chargeRepository, times(1)).save(mockCharge);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao tentar pagar cobrança já PAGA")
    void addPayment_ShouldThrowIllegalStateException_WhenChargeIsAlreadyPaid() {
        // Arrange
        mockCharge.setStatus(ChargeStatus.PAID);
        mockFindChargeOrFail();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> paymentService.addPayment(customerId, orderId, employeeId, mockRequestDTO));

        verify(paymentRepository, never()).save(any());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o Pedido não for encontrado")
    void addPayment_ShouldThrowEntityNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.addPayment(customerId, orderId, employeeId, mockRequestDTO));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o Funcionário não for encontrado")
    void addPayment_ShouldThrowEntityNotFoundException_WhenEmployeeNotFound() {
        // Arrange
        mockFindChargeOrFail();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.addPayment(customerId, orderId, employeeId, mockRequestDTO));
    }

    // --- Testes para updatePayment ---

    @Test
    @DisplayName("Deve atualizar múltiplos campos do pagamento")
    void updatePayment_ShouldUpdateAllFields_WhenAllFieldsAreProvided() {
        // Arrange
        mockFindChargeOrFail();
        mockFindPaymentOrFail();
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(paymentMapper.toResponseDTO(mockPayment)).thenReturn(mockResponseDTO);

        // Act
        paymentService.updatePayment(customerId, orderId, paymentId, mockPatchDTO);

        // Assert
        assertEquals(mockPatchDTO.getPaidAmount(), mockPayment.getPaidAmount());
        assertEquals(mockPatchDTO.getPaymentMethod(), mockPayment.getPaymentMethod());
        verify(paymentRepository, times(1)).save(mockPayment);
        verify(chargeRepository, times(1)).save(mockCharge);
    }

    @Test
    @DisplayName("Não deve atualizar campos quando valores são nulos no Patch")
    void updatePayment_ShouldNotUpdateFields_WhenValuesAreNull() {
        // Arrange
        String originalMethod = mockPayment.getPaymentMethod();
        BigDecimal originalAmount = mockPayment.getPaidAmount();

        PaymentPatchRequestDTO nullPatchDTO = PaymentPatchRequestDTO.builder().build();

        mockFindChargeOrFail();
        mockFindPaymentOrFail();
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        // Act
        paymentService.updatePayment(customerId, orderId, paymentId, nullPatchDTO);

        // Assert
        assertEquals(originalAmount, mockPayment.getPaidAmount());
        assertEquals(originalMethod, mockPayment.getPaymentMethod());
        verify(paymentRepository, times(1)).save(mockPayment);
    }


    @Test
    @DisplayName("Deve atualizar status da cobrança para PAGO após update do pagamento")
    void updatePayment_ShouldUpdateChargeStatusToPaid_WhenUpdateCoversTotal() {
        // Arrange
        PaymentPatchRequestDTO fullPaymentPatch = PaymentPatchRequestDTO.builder()
                .paidAmount(new BigDecimal("100.00"))
                .build();

        mockFindChargeOrFail();
        mockFindPaymentOrFail();
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        // Act
        paymentService.updatePayment(customerId, orderId, paymentId, fullPaymentPatch);

        // Assert
        assertEquals(new BigDecimal("100.00"), mockPayment.getPaidAmount());
        assertEquals(ChargeStatus.UNPAID, mockCharge.getStatus());
        verify(chargeRepository, times(1)).save(mockCharge);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o Pagamento não for encontrado")
    void updatePayment_ShouldThrowEntityNotFoundException_WhenPaymentNotFound() {
        // Arrange
        mockFindChargeOrFail();
        when(paymentRepository.findByIdAndChargeId(paymentId, chargeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.updatePayment(customerId, orderId, paymentId, mockPatchDTO));
    }

    // --- Testes para deletePayment ---

    @Test
    @DisplayName("Deve deletar o pagamento e atualizar status para NÃO PAGO")
    void deletePayment_ShouldDeletePaymentAndUpdateChargeStatusToUnpaid() {
        // Arrange
        mockCharge.getPayments().add(mockPayment);
        mockCharge.setStatus(ChargeStatus.PARTIAL);

        mockFindChargeOrFail();
        mockFindPaymentOrFail();
        doNothing().when(paymentRepository).delete(mockPayment);

        // Act
        paymentService.deletePayment(customerId, orderId, paymentId);

        // Assert
        verify(paymentRepository, times(1)).delete(mockPayment);
        assertFalse(mockCharge.getPayments().contains(mockPayment));
        assertEquals(ChargeStatus.UNPAID, mockCharge.getStatus());
        verify(chargeRepository, times(1)).save(mockCharge);
    }

    @Test
    @DisplayName("Deve deletar um pagamento e manter status PARCIAL se houver outros")
    void deletePayment_ShouldDeletePaymentAndKeepStatusPartial() {
        // Arrange
        Payment anotherPayment = Payment.builder()
                .id(2L)
                .paidAmount(new BigDecimal("25.00"))
                .build();

        mockCharge.getPayments().add(mockPayment);
        mockCharge.getPayments().add(anotherPayment);
        mockCharge.setStatus(ChargeStatus.PARTIAL);

        mockFindChargeOrFail();
        mockFindPaymentOrFail();
        doNothing().when(paymentRepository).delete(mockPayment);

        // Act
        paymentService.deletePayment(customerId, orderId, paymentId);

        // Assert
        verify(paymentRepository, times(1)).delete(mockPayment);
        assertFalse(mockCharge.getPayments().contains(mockPayment));
        assertTrue(mockCharge.getPayments().contains(anotherPayment));
        assertEquals(ChargeStatus.PARTIAL, mockCharge.getStatus());
        verify(chargeRepository, times(1)).save(mockCharge);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar deletar pagamento inexistente")
    void deletePayment_ShouldThrowEntityNotFoundException_WhenPaymentNotFound() {
        // Arrange
        mockFindChargeOrFail();
        when(paymentRepository.findByIdAndChargeId(paymentId, chargeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.deletePayment(customerId, orderId, paymentId));

        verify(paymentRepository, never()).delete(any());
        verify(chargeRepository, never()).save(any());
    }
}