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
import com.nexora.elegance.models.CartItem;
import com.nexora.elegance.models.Order;
import com.nexora.elegance.databinding.ActivityOrderDetailsBinding;
import com.nexora.elegance.adapters.OrderDetailsAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * OrderDetailsActivity displays the full details of a past order.
 * Key features:
 * - Order summary (Status, Date, Address, Total).
 * - List of items purchased with their specific variants.
 * - PDF Receipt Generation: Creates a professional-looking invoice saved to
 * local storage.
 */
public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private Order currentOrder;
    private OrderDetailsAdapter adapter;
    private com.google.firebase.firestore.ListenerRegistration orderListener;
    private android.content.BroadcastReceiver orderUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Extract Order object from the starting Intent
        currentOrder = (Order) getIntent().getSerializableExtra("order");
        String orderId = getIntent().getStringExtra("orderId");

        if (currentOrder == null && orderId != null) {
            fetchOrderDetails(orderId);
        } else if (currentOrder == null) {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            setupUI();
            setupListeners();
            startRealTimeUpdates();
            setupBroadcastReceiver();
        }
    }

    private void fetchOrderDetails(String orderId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to view order details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            setupUI();
                            setupListeners();
                            startRealTimeUpdates();
                            setupBroadcastReceiver();
                        } else {
                            Toast.makeText(this, "Failed to parse order details", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void startRealTimeUpdates() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentOrder.getOrderId() == null) return;

        orderListener = FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("orders").document(currentOrder.getOrderId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && value.exists()) {
                        Order updatedOrder = value.toObject(Order.class);
                        if (updatedOrder != null) {
                            currentOrder = updatedOrder;
                            updateStatusUI();
                        }
                    }
                });
    }

    private void setupBroadcastReceiver() {
        orderUpdateReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, android.content.Intent intent) {
                String receivedOrderId = intent.getStringExtra("orderId");
                if (receivedOrderId == null || receivedOrderId.equals(currentOrder.getOrderId())) {
                    Toast.makeText(OrderDetailsActivity.this, "Order status updated!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .registerReceiver(orderUpdateReceiver, new android.content.IntentFilter("com.nexora.elegance.ORDER_UPDATED"));
    }

    private void updateStatusUI() {
        binding.detailStatusText.setText(currentOrder.getStatus() != null ? currentOrder.getStatus() : "Processing");
        if (binding.detailStatusText.getText().toString().equalsIgnoreCase("Completed")) {
            binding.detailStatusText.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else if (binding.detailStatusText.getText().toString().equalsIgnoreCase("Cancelled")) {
            binding.detailStatusText.setBackgroundColor(android.graphics.Color.parseColor("#F44336")); // Red
        } else {
            // Default background for other statuses
            binding.detailStatusText.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    private void setupUI() {
        binding.detailOrderIdText.setText("Order ID: #" + currentOrder.getOrderId());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        binding.detailDateText.setText("Placed on: " + sdf.format(new Date(currentOrder.getTimestamp())));

        // Display status with color feedback
        updateStatusUI();

        binding.detailAddressText.setText(
                currentOrder.getShippingAddress() != null ? currentOrder.getShippingAddress() : "No address provided");
        binding.detailTotalAmountText
                .setText(String.format(Locale.getDefault(), "LKR %.2f", currentOrder.getTotalAmount()));

        // Bind items to the RecyclerView
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
        if (orderUpdateReceiver != null) {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(orderUpdateReceiver);
        }
    }

    /**
     * Programmatically generates a PDF invoice for the current order.
     * Uses Android's PdfDocument and Canvas API to draw the layout.
     * The file is saved to the public Downloads folder.
     */
    private void generatePdfSlip() {
        if (currentOrder == null)
            return;

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Standard A4 dimensions (roughly)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Render Title/Branding
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(24);
        titlePaint.setColor(Color.parseColor("#E91E63")); // Elegance Accent Color
        canvas.drawText("Elegance - Order Receipt", 40, 60, titlePaint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(14);
        paint.setColor(Color.parseColor("#333333"));

        // Render Header Metadata
        int y = 100;
        canvas.drawText("Order ID: " + currentOrder.getOrderId(), 40, y, paint);
        y += 25;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        canvas.drawText("Date: " + sdf.format(new Date(currentOrder.getTimestamp())), 40, y, paint);
        y += 25;

        canvas.drawText("Status: " + (currentOrder.getStatus() != null ? currentOrder.getStatus() : "Processing"), 40,
                y, paint);
        y += 40;

        titlePaint.setTextSize(18);
        titlePaint.setColor(Color.parseColor("#111111"));
        canvas.drawText("Items Purchased:", 40, y, titlePaint);
        y += 25;

        // Populate Table Rows with Items
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

            paint.setColor(Color.parseColor("#666666"));
            paint.setTextSize(12);
            canvas.drawText(specs, 60, y, paint);

            paint.setColor(Color.parseColor("#333333"));
            paint.setTextSize(14);
            y += 30;
        }

        // Horizontal Separator
        y += 20;
        canvas.drawLine(40, y, 555, y, paint);
        y += 30;

        // Order Total
        titlePaint.setTextSize(20);
        titlePaint.setColor(Color.parseColor("#E91E63"));
        canvas.drawText(String.format(Locale.getDefault(), "Total Amount: LKR %.2f", currentOrder.getTotalAmount()), 40,
                y, titlePaint);

        pdfDocument.finishPage(page);

        // Save file to system Downloads directory
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
