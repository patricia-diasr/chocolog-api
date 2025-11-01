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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ChargeRepository chargeRepository;
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponseDTO addPayment(Long customerId, Long orderId, Long employeeId, PaymentRequestDTO paymentDTO) {
        Charge charge = findChargeOrFail(orderId, customerId);

        if (charge.getStatus() == ChargeStatus.PAID) {
            throw new IllegalStateException("This charge is already paid.");
        }

        Employee employee = findEmployeeOrFail(employeeId);

        Payment payment = new Payment();
        payment.setCharge(charge);
        payment.setEmployee(employee);
        payment.setPaidAmount(paymentDTO.getPaidAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setPaymentDate(paymentDTO.getPaymentDate());
        Payment savedPayment = paymentRepository.save(payment);

        charge.getPayments().add(savedPayment);
        updateChargeStatus(charge);

        return paymentMapper.toResponseDTO(savedPayment);
    }

    @Transactional
    public PaymentResponseDTO updatePayment(Long customerId, Long orderId, Long paymentId, PaymentPatchRequestDTO paymentDTO) {
        Charge charge = findChargeOrFail(orderId, customerId);
        Payment payment = findPaymentOrFail(paymentId, charge.getId());

        if (paymentDTO.getPaidAmount() != null) {
            payment.setPaidAmount(paymentDTO.getPaidAmount());
        }
        if (paymentDTO.getPaymentMethod() != null) {
            payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        }
        if (paymentDTO.getPaymentDate() != null) {
            payment.setPaymentDate(paymentDTO.getPaymentDate());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        updateChargeStatus(charge);

        return paymentMapper.toResponseDTO(updatedPayment);
    }

    @Transactional
    public void deletePayment(Long customerId, Long orderId, Long paymentId) {
        Charge charge = findChargeOrFail(orderId, customerId);
        Payment payment = findPaymentOrFail(paymentId, charge.getId());

        paymentRepository.delete(payment);
        charge.getPayments().remove(payment);

        updateChargeStatus(charge);
    }

    private void updateChargeStatus(Charge charge) {
        BigDecimal totalPaid = charge.getPayments().stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(charge.getTotalAmount()) >= 0) {
            charge.setStatus(ChargeStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            charge.setStatus(ChargeStatus.PARTIAL);
        } else {
            charge.setStatus(ChargeStatus.UNPAID);
        }

        chargeRepository.save(charge);
    }

    private Charge findChargeOrFail(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id: " + orderId + " and customer id: " + customerId));

        return chargeRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new EntityNotFoundException("Charge not found for order id: " + orderId));
    }

    private Payment findPaymentOrFail(Long paymentId, Long chargeId) {
        return paymentRepository.findByIdAndChargeId(paymentId, chargeId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for id: " + paymentId + " and charge id: " + chargeId));
    }

    private Employee findEmployeeOrFail(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for id: " + employeeId));
    }
}