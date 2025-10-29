package com.chocolog.api.service;

import com.chocolog.api.model.*;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color PRIMARY_COLOR = new DeviceRgb(31, 143, 143);
    private static final Color ACCENT_COLOR = new DeviceRgb(161, 15, 48);
    private static final Color GRAY = new DeviceRgb(90, 90, 90);
    private static final Color BLACK = new DeviceRgb(0, 0, 0);

    public byte[] generateBatchPdf(PrintBatch batch) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        document.setMargins(0, 0, 0, 0);

        PdfFont regularFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont monoFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.COURIER);

        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        List<PrintBatchItem> items = batch.getItems();
        for (int i = 0; i < items.size(); i++) {
            PrintBatchItem item = items.get(i);
            Cell ticketCell = buildTicketCell(item, batch, regularFont, boldFont, monoFont);
            mainTable.addCell(ticketCell);

            if (i == items.size() - 1 && items.size() % 2 != 0) {
                mainTable.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        }

        document.add(mainTable);
        document.close();
        return baos.toByteArray();
    }

    private Cell buildTicketCell(PrintBatchItem item, PrintBatch batch,
                                 PdfFont regularFont, PdfFont boldFont, PdfFont monoFont) {

        OrderItem orderItem = item.getOrderItem();
        Order order = orderItem.getOrder();
        Customer customer = order.getCustomer();

        Cell cell = new Cell();
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(10);
        cell.setKeepTogether(true);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(GRAY, 0.5f));
        cell.setBorderRight(new com.itextpdf.layout.borders.SolidBorder(GRAY, 0.5f));

        Paragraph pName = new Paragraph();
        pName.add(new Text(customer.getName())
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(BLACK));
        if (customer.getIsReseller()) {
            pName.add(new Text(" (Revendedor)")
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setFontColor(PRIMARY_COLOR));
        }
        pName.setMarginBottom(3);
        cell.add(pName);

        String formattedPhone = formatPhone(Objects.toString(customer.getPhone(), ""));
        if (formattedPhone != null && !formattedPhone.isEmpty()) {
            Paragraph pPhone = new Paragraph(formattedPhone)
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setFontColor(GRAY);
            cell.add(pPhone);
        }

        String flavorStr = orderItem.getFlavor1().getName();
        if (orderItem.getFlavor2() != null) {
            flavorStr += " / " + orderItem.getFlavor2().getName();
        }

        String itemDesc = String.format("%dx %s - %s",
                orderItem.getQuantity(),
                orderItem.getSize().getName(),
                flavorStr);
        Paragraph pItem = new Paragraph(itemDesc)
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(ACCENT_COLOR)
                .setMarginTop(8);
        cell.add(pItem);

        if (orderItem.getNotes() != null && !orderItem.getNotes().isEmpty()) {
            Paragraph pNotes = new Paragraph(orderItem.getNotes())
                    .setFont(regularFont)
                    .setFontSize(11.5f)
                    .setFontColor(GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(4)
                    .setMarginBottom(2);
            cell.add(pNotes);
        }

        String dateStr = "Data: " + (order.getExpectedPickupDate() != null
                ? order.getExpectedPickupDate().format(DATE_FORMATTER)
                : "N/A");
        Paragraph pDate = new Paragraph(dateStr)
                .setFont(regularFont)
                .setFontSize(11)
                .setFontColor(BLACK)
                .setMarginTop(8);
        cell.add(pDate);

        String ids = String.format("C:%d | P:%d | I:%d | L:%d",
                customer.getId(),
                order.getId(),
                orderItem.getId(),
                batch.getId());
        Paragraph pIds = new Paragraph(ids)
                .setFont(monoFont)
                .setFontSize(7)
                .setFontColor(GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5);
        cell.add(pIds);

        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        return cell;
    }

    private String formatPhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 7),
                    digits.substring(7));
        } else if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 6),
                    digits.substring(6));
        }
        return phone;
    }
}
