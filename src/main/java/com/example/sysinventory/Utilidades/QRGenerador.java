package com.example.sysinventory.Utilidades;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class QRGenerador {

    private static final int ANCHO  = 300;
    private static final int ALTO   = 300;

    // CAMBIADO: Ahora apunta al endpoint de redirección a SharePoint
    private static final String BASE_URL = "https://inventario-ti.onrender.com/redirect/";

    public String generarBase64(String codigo) throws WriterException, IOException {
        String url = BASE_URL + codigo;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, ANCHO, ALTO);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public byte[] generarBytes(String codigo) throws WriterException, IOException {
        String url = BASE_URL + codigo;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, ANCHO, ALTO);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}