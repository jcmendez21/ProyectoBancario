package com.pruebatecnica.api.controller;

import com.pruebatecnica.etl.service.DataExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/etl")
public class ETLController {

    @Autowired
    private DataExtractionService dataExtractionService;

    // POST /api/etl/run - Ejecutar proceso ETL (requiere token)
    @PostMapping("/run")
    public ResponseEntity<String> runETL() {
        String result = dataExtractionService.extractAndLoadData();
        return ResponseEntity.ok(result);
    }

    // GET /api/etl/run - Ejecutar ETL desde navegador (para pruebas)
    @GetMapping("/run")
    public ResponseEntity<String> runETLGet() {
        String result = dataExtractionService.extractAndLoadData();
        return ResponseEntity.ok("🚀 " + result);
    }

    // GET /api/etl/status - Obtener estado actual
    @GetMapping("/status")
    public ResponseEntity<Object> getETLStatus() {
        String status = dataExtractionService.getETLStatus();
        long totalRecords = dataExtractionService.getTotalRecordsCount();
        
        // Crear un objeto simple para la respuesta
        ETLStatusResponse response = new ETLStatusResponse();
        response.message = status;
        response.totalRecords = totalRecords;
        response.apiUrl = "http://localhost:8081/api/data";
        response.h2Console = "http://localhost:8081/api/h2-console";
        
        return ResponseEntity.ok(response);
    }
    
    // Clase interna para la respuesta del status
    public static class ETLStatusResponse {
        public String message;
        public long totalRecords;
        public String apiUrl;
        public String h2Console;
    }
}