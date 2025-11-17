package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class BusQRCodeController {

    private final QRCodeService qrCodeService;

    public BusQRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/{busId}/{empresaId}")
    public ResponseEntity<byte[]> getBusQRCode(@PathVariable String busId, @PathVariable String empresaId) {
        // Generar datos básicos para el QR
        String qrContent = String.format("busId=%s&empresaId=%s", busId, empresaId);
        byte[] qrImage = qrCodeService.generateQRCode(qrContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // Define que es una imagen PNG
        return new ResponseEntity<>(qrImage, headers, HttpStatus.OK);
    }
}