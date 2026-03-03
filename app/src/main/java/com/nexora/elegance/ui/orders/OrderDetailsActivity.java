package com.nexora.elegance.ui.orders;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.data.models.CartItem;
import com.nexora.elegance.data.models.Order;
import com.nexora.elegance.databinding.ActivityOrderDetailsBinding;
import com.nexora.elegance.ui.adapters.OrderDetailsAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private Order currentOrder;
    private OrderDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentOrder = (Order) getIntent().getSerializableExtra("order");
        if (currentOrder == null) {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        // Top Order Info block
        binding.detailOrderIdText.setText("Order ID: #" + currentOrder.getOrderId());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        binding.detailDateText.setText("Placed on: " + sdf.format(new Date(currentOrder.getTimestamp())));

        binding.detailStatusText.setText(currentOrder.getStatus() != null ? currentOrder.getStatus() : "Processing");
        if (binding.detailStatusText.getText().toString().equalsIgnoreCase("Completed")) {
            binding.detailStatusText.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else if (binding.detailStatusText.getText().toString().equalsIgnoreCase("Cancelled")) {
            binding.detailStatusText.setBackgroundColor(android.graphics.Color.parseColor("#F44336")); // Red
        }

        binding.detailAddressText.setText(
                currentOrder.getShippingAddress() != null ? currentOrder.getShippingAddress() : "No address provided");
        binding.detailTotalAmountText
                .setText(String.format(Locale.getDefault(), "LKR %.2f", currentOrder.getTotalAmount()));

        // Setup generic items recycler seamlessly pushing the list to adapter mapping
        // bounds
        adapter = new OrderDetailsAdapter(this, currentOrder.getItems());
        binding.detailItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.detailItemsRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnDownloadPdf.setOnClickListener(v -> {
            Toast.makeText(this, "Generating PDF Slip...", Toast.LENGTH_SHORT).show();
            generatePdfSlip();
        });
    }

    private void generatePdfSlip() {
        if (currentOrder == null)
            return;

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Setup page info (A4 size roughly represented)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw Title
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(24);
        titlePaint.setColor(Color.parseColor("#E91E63")); // Elegance Primary
        canvas.drawText("Elegance - Order Receipt", 40, 60, titlePaint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(14);
        paint.setColor(Color.parseColor("#333333"));

        // Draw Header Information
        int y = 100;
        canvas.drawText("Order ID: " + currentOrder.getOrderId(), 40, y, paint);
        y += 25;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        canvas.drawText("Date: " + sdf.format(new Date(currentOrder.getTimestamp())), 40, y, paint);
        y += 25;

        canvas.drawText("Status: " + (currentOrder.getStatus() != null ? currentOrder.getStatus() : "Processing"), 40,
                y, paint);
        y += 40;

        // Draw Items Header
        titlePaint.setTextSize(18);
        titlePaint.setColor(Color.parseColor("#111111"));
        canvas.drawText("Items Purchased:", 40, y, titlePaint);
        y += 25;

        // Draw Items Loop
        for (CartItem item : currentOrder.getItems()) {
            canvas.drawText("- " + item.getName(), 60, y, paint);
            y += 20;

            String specs = String.format(Locale.getDefault(), "  LKR %.2f x %d", item.getPrice(),
                    Math.max(1, item.getQuantity()));
            if (item.getSize() != null && !item.getSize().isEmpty() && !item.getSize().equals("Default")) {
                specs += " | Size: " + item.getSize();
            }
            if (item.getColor() != null && !item.getColor().isEmpty() && !item.getColor().equals("Default")) {
                specs += " | Color: " + item.getColor();
            }

            paint.setColor(Color.parseColor("#666666")); // Lighter for specs
            paint.setTextSize(12);
            canvas.drawText(specs, 60, y, paint);

            // Reset paint
            paint.setColor(Color.parseColor("#333333"));
            paint.setTextSize(14);
            y += 30;
        }

        // Draw Footer
        y += 20;
        canvas.drawLine(40, y, 555, y, paint);
        y += 30;

        titlePaint.setTextSize(20);
        titlePaint.setColor(Color.parseColor("#E91E63"));
        canvas.drawText(String.format(Locale.getDefault(), "Total Amount: LKR %.2f", currentOrder.getTotalAmount()), 40,
                y, titlePaint);

        pdfDocument.finishPage(page);

        // Save aggressively into external public endpoints without explicitly managing
        // scope requests natively in 30+
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Elegance_Order_" + currentOrder.getOrderId().substring(0, 8) + ".pdf";
        File file = new File(downloadsDir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Order slip saved to Downloads folder!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}
