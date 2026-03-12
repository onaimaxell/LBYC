package com.dasalla.pos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeService {

    public Image generateQRCodeImage(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            return new Image(bais);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image generateOrderLookupQR(String orderNumber, int size) {
        String content = "DASALLA-ORDER:" + orderNumber;
        return generateQRCodeImage(content, size, size);
    }

    /**
     * TODO (lucas)
     * Replace gcashQRContent with the actual GCash merchant QR data.
     */
    public Image getGCashQRCode(int size) {
        String gcashQRContent = "https://gcash.com/merchant/dasallalaundryshop";
        return generateQRCodeImage(gcashQRContent, size, size);
    }
}
