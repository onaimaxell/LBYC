package com.dasalla.pos.service;

public class NotificationService {

    /**
     * In progress (simulation only)
     */
    public void sendCompletionSMS(String phoneNumber, String orderNumber, String customerName) {
        String message = String.format(
            "Hello %s! Your laundry order #%s from Dasalla Laundry Shop is now ready for pickup. Thank you!",
            customerName, orderNumber
        );
        sendSMS(phoneNumber, message);
    }

    //
    public void sendOrderReceivedSMS(String phoneNumber, String orderNumber, String customerName) {
        String message = String.format(
            "Hi %s! We received your laundry order #%s at Dasalla Laundry Shop. We'll notify you when it's ready!",
            customerName, orderNumber
        );
        sendSMS(phoneNumber, message);
    }

    private void sendSMS(String phoneNumber, String message) {
        // ============================================================
        // Example using Semaphore
        //
        // HttpClient client = HttpClient.newHttpClient();
        // String body = "apikey=YOUR_API_KEY"
        //     + "&number=" + URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
        //     + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
        //     + "&sendername=DasallaLaundry";
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create("https://api.semaphore.co/api/v4/messages"))
        //     .header("Content-Type", "application/x-www-form-urlencoded")
        //     .POST(HttpRequest.BodyPublishers.ofString(body))
        //     .build();
        // HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //
        //  Credits: https://semaphore.co/docs
        // ============================================================

        System.out.println("=== SMS NOTIFICATION (SIMULATED) ===");
        System.out.println("TO: " + phoneNumber);
        System.out.println("MESSAGE: " + message);
        System.out.println("=====================================");
    }
}
