package com.example.doan.Service.order;

import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.Invoice;
import com.example.doan.Repository.order.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


@Service
public class Invoice_Service {

    private final InvoiceRepository invoiceRepo;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Invoice_Service(InvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    /**
     * Tạo hóa đơn tự động cho đơn hàng đã thanh toán thành công.
     */
    @Transactional
    public Invoice Generate_Invoice_For_Order(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ!");
        }

        // Kiểm tra xem đã có hóa đơn cho đơn này chưa
        Optional<Invoice> existing = invoiceRepo.findByOrderId(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Tạo số hóa đơn duy nhất: INV-YYYYMMDD-OrderID-Rand4
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        int rand = new Random().nextInt(9000) + 1000; // 1000 -> 9999
        String soHoaDon = "INV-" + dateStr + "-" + order.getId() + "-" + rand;

        Invoice invoice = new Invoice(order, soHoaDon);
        return invoiceRepo.save(invoice);
    }

    /**
     * Tìm hóa đơn theo đơn hàng.
     */
    public Optional<Invoice> Get_Invoice_By_Order(Long orderId) {
        return invoiceRepo.findByOrderId(orderId);
    }
}
