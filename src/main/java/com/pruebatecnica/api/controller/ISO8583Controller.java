package com.pruebatecnica.api.controller;

import com.pruebatecnica.jpos.service.ISO8583Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/iso8583")
public class ISO8583Controller {

    @Autowired
    private ISO8583Service iso8583Service;

    @GetMapping("/demo")
    public ResponseEntity<DemoResponse> runDemo() {
        DemoResponse demo = new DemoResponse();
        
        String authMessage = iso8583Service.createAuthorizationRequest(
            "4111111111111111", "25000", "DEMO001"
        );
        demo.step1_authRequest = authMessage;
        demo.step1_description = "Solicitud de autorización por $250.00";
        
        String approvedResponse = iso8583Service.createAuthorizationResponse(authMessage, "00");
        demo.step2_authResponse = approvedResponse;
        demo.step2_description = "Respuesta: APROBADA";
        
        ISO8583Service.ISO8583MessageInfo authInfo = iso8583Service.parseMessage(authMessage);
        demo.step3_parsedInfo = authInfo;
        demo.step3_description = "Información extraída del mensaje";
        
        ISO8583Service.ISO8583MessageInfo responseInfo = iso8583Service.parseMessage(approvedResponse);
        demo.step4_responseInfo = responseInfo;
        demo.step4_description = "Información de la respuesta";
        
        demo.summary = "Demostración completa: Autorización solicitada y aprobada";
        
        return ResponseEntity.ok(demo);
    }

    @GetMapping("/test-auth")
    public ResponseEntity<ISO8583Response> createTestAuthorization() {
        String message = iso8583Service.createAuthorizationRequest(
            "4111111111111111", 
            "50000", 
            "TERM001"
        );
        
        ISO8583Response response = new ISO8583Response();
        response.success = true;
        response.message = "Mensaje de prueba creado - Autorización por $500.00";
        response.isoMessage = message;
        response.messageType = "0100 - Authorization Request";
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authorization")
    public ResponseEntity<ISO8583Response> createAuthorization(@RequestBody AuthRequest request) {
        String message = iso8583Service.createAuthorizationRequest(
            request.getPan(), 
            request.getAmount(), 
            request.getMerchantId()
        );
        
        ISO8583Response response = new ISO8583Response();
        response.success = true;
        response.message = "Mensaje de autorización creado";
        response.isoMessage = message;
        response.messageType = "0100 - Authorization Request";
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approve")
    public ResponseEntity<ISO8583Response> approveTransaction(@RequestParam String msg) {
        String responseMessage = iso8583Service.createAuthorizationResponse(msg, "00");
        
        ISO8583Response response = new ISO8583Response();
        response.success = true;
        response.message = "Transacción APROBADA";
        response.isoMessage = responseMessage;
        response.messageType = "0110 - Authorization Response";
        response.responseCode = "00";
        response.responseDescription = "Approved";
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/parse")
    public ResponseEntity<Object> parseMessage(@RequestBody ParseRequest request) {
        ISO8583Service.ISO8583MessageInfo info = iso8583Service.parseMessage(request.getMessage());
        
        if (info == null) {
            ISO8583Response errorResponse = new ISO8583Response();
            errorResponse.success = false;
            errorResponse.message = "Error parseando mensaje ISO 8583";
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        ParseResponse response = new ParseResponse();
        response.success = true;
        response.message = "Mensaje parseado exitosamente";
        response.messageInfo = info;
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ISO8583Response> validateMessage(@RequestBody ParseRequest request) {
        boolean isValid = iso8583Service.validateMessage(request.getMessage());
        
        ISO8583Response response = new ISO8583Response();
        response.success = isValid;
        response.message = isValid ? "Mensaje válido" : "Mensaje inválido";
        
        return ResponseEntity.ok(response);
    }

    public static class AuthRequest {
        private String pan;
        private String amount;
        private String merchantId;

        public String getPan() { return pan; }
        public void setPan(String pan) { this.pan = pan; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    }

    public static class ParseRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ISO8583Response {
        public boolean success;
        public String message;
        public String isoMessage;
        public String messageType;
        public String responseCode;
        public String responseDescription;
    }

    public static class ParseResponse {
        public boolean success;
        public String message;
        public ISO8583Service.ISO8583MessageInfo messageInfo;
    }

    public static class DemoResponse {
        public String step1_authRequest;
        public String step1_description;
        public String step2_authResponse;
        public String step2_description;
        public ISO8583Service.ISO8583MessageInfo step3_parsedInfo;
        public String step3_description;
        public ISO8583Service.ISO8583MessageInfo step4_responseInfo;
        public String step4_description;
        public String summary;
    }
}