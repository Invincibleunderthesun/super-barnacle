package com.harsh.uday.service;

import com.harsh.uday.exception.ApiException;
import com.harsh.uday.model.Order;
import com.harsh.uday.model.Payment;
import com.harsh.uday.repository.OrderRepository;
import com.harsh.uday.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment service for Razorpay integration.
 * Uses placeholder keys by default — replace with real keys for live payments.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:placeholder_secret}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    /**
     * Create a Razorpay payment order for the given order.
     * Returns the Razorpay order details for the frontend to initiate payment.
     */
    public Map<String, Object> createPaymentOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        // Check if payment already exists
        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new ApiException("Payment already initiated for this order", HttpStatus.CONFLICT);
        }

        if (order.getStatus() != Order.Status.PAYMENT_PENDING) {
            throw new ApiException("Order is not in payment pending state", HttpStatus.BAD_REQUEST);
        }

        // Amount in paise (Razorpay uses smallest currency unit)
        long amountInPaise = Math.round(order.getTotalAmount() * 100);

        // In production with real Razorpay keys, you'd call:
        // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        // JSONObject options = new JSONObject();
        // options.put("amount", amountInPaise);
        // options.put("currency", "INR");
        // options.put("receipt", "order_" + orderId);
        // com.razorpay.Order razorpayOrder = client.orders.create(options);

        // For now, generate a mock Razorpay order ID
        String razorpayOrderId = "order_" + System.currentTimeMillis() + "_" + orderId;

        Payment payment = new Payment(order, razorpayOrderId, order.getTotalAmount());
        paymentRepository.save(payment);

        logger.info("Payment order created: {} for order #{}, amount: ₹{}",
                razorpayOrderId, orderId, order.getTotalAmount());

        // Return data needed by frontend to open Razorpay checkout
        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", razorpayOrderId);
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("razorpayKeyId", razorpayKeyId);
        response.put("orderId", orderId);
        response.put("description", "Order #" + orderId);

        return response;
    }

    /**
     * Verify payment after the frontend completes Razorpay checkout.
     * Validates the Razorpay signature and marks the order as PAID.
     */
    public Payment verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ApiException("Payment not found", HttpStatus.NOT_FOUND));

        // Verify signature: HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, secret)
        boolean isValid = verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (!isValid) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new ApiException("Payment verification failed — invalid signature", HttpStatus.BAD_REQUEST);
        }

        // Mark payment as successful
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update order status to PAID
        Order order = payment.getOrder();
        order.setStatus(Order.Status.PAID);
        orderRepository.save(order);

        // Send confirmation email
        if (order.getUser() != null) {
            emailService.sendOrderConfirmationEmail(
                    order.getUser().getEmail(), order.getId(), order.getTotalAmount());
        }

        logger.info("Payment verified and order #{} marked as PAID", order.getId());
        return payment;
    }

    /**
     * Initiate a refund for a paid order (admin only).
     */
    public Payment initiateRefund(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ApiException("No payment found for this order", HttpStatus.NOT_FOUND));

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new ApiException("Can only refund paid orders", HttpStatus.BAD_REQUEST);
        }

        // In production: call Razorpay refund API
        // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        // JSONObject refundRequest = new JSONObject();
        // refundRequest.put("amount", Math.round(payment.getAmount() * 100));
        // Refund refund = client.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

        String mockRefundId = "rfnd_" + System.currentTimeMillis();

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundId(mockRefundId);
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        logger.info("Refund initiated for order #{}: refundId={}", orderId, mockRefundId);
        return payment;
    }

    /**
     * Verify Razorpay signature using HMAC-SHA256.
     */
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            logger.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get payment details by order ID.
     */
    public Payment getPaymentByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        return paymentRepository.findByOrder(order).orElse(null);
    }
}
