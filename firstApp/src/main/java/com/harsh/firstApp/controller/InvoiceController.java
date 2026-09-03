package com.harsh.firstApp.controller;

import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.security.JwtFilter;
import com.harsh.firstApp.service.InvoiceService;
import com.harsh.firstApp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Invoices", description = "Invoice generation APIs")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;
    private final JwtFilter jwtFilter;

    public InvoiceController(InvoiceService invoiceService,
            OrderService orderService,
            JwtFilter jwtFilter) {
        this.invoiceService = invoiceService;
        this.orderService = orderService;
        this.jwtFilter = jwtFilter;
    }

    @Operation(summary = "Download invoice PDF for an order")
    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
        }

        // Verify ownership (user can only download their own invoice, admin can download any)
        if (!JwtFilter.isAdmin() && order.getUser() != null) {
            Long currentUserId = jwtFilter.getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(order.getUser().getId())) {
                throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
            }
        }

        byte[] pdfBytes = invoiceService.generateInvoice(order);

        String filename = (order.getInvoiceNumber() != null ? order.getInvoiceNumber() : "invoice_" + orderId) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
