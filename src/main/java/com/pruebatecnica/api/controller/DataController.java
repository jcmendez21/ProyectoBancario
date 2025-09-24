package com.pruebatecnica.api.controller;

import com.pruebatecnica.entity.DataRecord;
import com.pruebatecnica.repository.DataRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataRecordRepository dataRecordRepository;

    @GetMapping
    public ResponseEntity<Page<DataRecord>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DataRecord> records = dataRecordRepository.findAll(pageable);

        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataRecord> getRecordById(@PathVariable Long id) {
        Optional<DataRecord> record = dataRecordRepository.findById(id);
        
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DataRecord>> searchRecords(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DataRecord> records = dataRecordRepository.findByKeyword(keyword, pageable);

        return ResponseEntity.ok(records);
    }

    @PostMapping
    public ResponseEntity<DataRecord> createRecord(@RequestBody DataRecord record) {
        DataRecord savedRecord = dataRecordRepository.save(record);
        return ResponseEntity.ok(savedRecord);
    }

    // Endpoint de prueba para insertar datos rápido
    @GetMapping("/test")
    public ResponseEntity<String> createTestRecordGet() {
        DataRecord record = new DataRecord();
        record.setTitle("Registro de prueba GET");
        record.setContent("Contenido de prueba desde navegador");
        record.setAuthorName("Sistema Test");
        record.setCategory("Test");
        record.setExternalId("test-" + System.currentTimeMillis());
        record.setSourceUrl("http://test.com");

        DataRecord saved = dataRecordRepository.save(record);
        return ResponseEntity.ok("✅ Registro creado con ID: " + saved.getId());
    }
}