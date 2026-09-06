package com.harsh.uday.controller;

import com.harsh.uday.dto.ApiResponse;
import com.harsh.uday.dto.PaymentVerifyRequest;
import com.harsh.uday.model.Payment;
import com.harsh.uday.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Razorpay payment integration APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Create a Razorpay payment order for an existing order")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create-order/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPaymentOrder(@PathVariable Long orderId) {
        Map<String, Object> paymentData = paymentService.createPaymentOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created", paymentData));
    }

    @Operation(summary = "Verify payment after Razorpay checkout (called by frontend)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        Payment payment = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
    }

    @Operation(summary = "Razorpay webhook endpoint (no auth required)")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        // In production: parse webhook events (payment.captured, refund.processed, etc.)
        // and update Payment/Order status accordingly.
        // For now, log and acknowledge.
        return ResponseEntity.ok("OK");
    }

    @Operation(summary = "Initiate refund for a paid order (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> initiateRefund(@PathVariable Long orderId) {
        Payment payment = paymentService.initiateRefund(orderId);
        return ResponseEntity.ok(ApiResponse.success("Refund initiated", payment));
    }

    @Operation(summary = "Get payment details by order ID")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrder(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved", payment));
    }
}
