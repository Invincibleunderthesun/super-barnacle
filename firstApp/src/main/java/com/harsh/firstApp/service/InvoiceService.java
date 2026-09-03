package com.harsh.firstApp.service;

import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.Order;
import com.harsh.firstApp.model.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;

/**
 * Generates PDF invoices for orders with GST breakdown.
 * Uses OpenPDF (free, open-source iText fork).
 */
@Service
public class InvoiceService {

    @Value("${app.store.name:Your E-Commerce Store}")
    private String storeName;

    @Value("${app.store.address:India}")
    private String storeAddress;

    @Value("${app.store.gstin:NOT REGISTERED}")
    private String storeGstin;

    /**
     * Generate a PDF invoice for the given order.
     */
    public byte[] generateInvoice(Order order) {
        if (order == null) {
            throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(33, 37, 41));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(33, 37, 41));
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(73, 80, 87));
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(33, 37, 41));
            Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(108, 117, 125));

            // === HEADER ===
            Paragraph title = new Paragraph("TAX INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Invoice details (2-column table)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            // Left: Seller info
            PdfPCell sellerCell = new PdfPCell();
            sellerCell.setBorder(0);
            sellerCell.addElement(new Paragraph(storeName, headerFont));
            sellerCell.addElement(new Paragraph(storeAddress, normalFont));
            sellerCell.addElement(new Paragraph("GSTIN: " + storeGstin, normalFont));
            infoTable.addCell(sellerCell);

            // Right: Invoice details
            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBorder(0);
            invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            invoiceCell.addElement(createRightAligned("Invoice #: " + (order.getInvoiceNumber() != null ? order.getInvoiceNumber() : "N/A"), headerFont));
            invoiceCell.addElement(createRightAligned("Date: " + (order.getOrderDate() != null ? order.getOrderDate().toLocalDate().toString() : "N/A"), normalFont));
            invoiceCell.addElement(createRightAligned("Status: " + order.getStatus(), normalFont));
            infoTable.addCell(invoiceCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // === SHIP TO ===
            if (order.getShippingName() != null) {
                Paragraph shipTo = new Paragraph("SHIP TO:", headerFont);
                document.add(shipTo);
                document.add(new Paragraph(order.getShippingName(), normalFont));
                document.add(new Paragraph(order.getShippingAddress(), normalFont));
                document.add(new Paragraph(order.getShippingCity() + ", " + order.getShippingState() + " - " + order.getShippingPincode(), normalFont));
                document.add(new Paragraph("Phone: " + order.getShippingPhone(), normalFont));
                document.add(new Paragraph(" "));
            }

            // === ITEMS TABLE ===
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 3f, 1f, 1f, 1.2f});

            // Header row
            Color headerBg = new Color(52, 58, 64);
            Color white = Color.WHITE;
            Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, white);

            addTableHeader(table, "#", tableHeaderFont, headerBg);
            addTableHeader(table, "Product", tableHeaderFont, headerBg);
            addTableHeader(table, "Price (₹)", tableHeaderFont, headerBg);
            addTableHeader(table, "Qty", tableHeaderFont, headerBg);
            addTableHeader(table, "Total (₹)", tableHeaderFont, headerBg);

            // Item rows
            int idx = 1;
            Color altBg = new Color(248, 249, 250);
            for (OrderItem item : order.getItems()) {
                Color bg = (idx % 2 == 0) ? altBg : Color.WHITE;
                addTableCell(table, String.valueOf(idx), normalFont, bg);
                addTableCell(table, item.getProductName(), normalFont, bg);
                addTableCell(table, String.format("%.2f", item.getPrice()), normalFont, bg);
                addTableCell(table, String.valueOf(item.getQuantity()), normalFont, bg);
                addTableCell(table, String.format("%.2f", item.getSubtotal()), boldFont, bg);
                idx++;
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // === TOTALS ===
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addTotalRow(totalsTable, "Subtotal:", String.format("₹%.2f", order.getSubtotal()), normalFont, boldFont);
            addTotalRow(totalsTable, String.format("GST (%.0f%%):", order.getTaxRate() * 100), String.format("₹%.2f", order.getTaxAmount()), normalFont, boldFont);

            // Grand total with highlight
            PdfPCell totalLabel = new PdfPCell(new Phrase("Total:", headerFont));
            totalLabel.setBorder(Rectangle.TOP);
            totalLabel.setPadding(5);
            totalsTable.addCell(totalLabel);

            Font totalValueFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(40, 167, 69));
            PdfPCell totalValue = new PdfPCell(new Phrase(String.format("₹%.2f", order.getTotalAmount()), totalValueFont));
            totalValue.setBorder(Rectangle.TOP);
            totalValue.setPadding(5);
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.addCell(totalValue);

            document.add(totalsTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // === FOOTER ===
            Paragraph footer = new Paragraph("Thank you for your purchase!", boldFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            Paragraph note = new Paragraph("This is a computer-generated invoice and does not require a signature.", smallFont);
            note.setAlignment(Element.ALIGN_CENTER);
            document.add(note);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Paragraph createRightAligned(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void addTableHeader(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(0);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(0);
        valueCell.setPadding(3);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}
