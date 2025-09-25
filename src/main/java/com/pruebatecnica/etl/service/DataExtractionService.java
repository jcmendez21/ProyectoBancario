package com.pruebatecnica.etl.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pruebatecnica.entity.DataRecord;
import com.pruebatecnica.repository.DataRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DataExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DataExtractionService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataRecordRepository dataRecordRepository;

    @Value("${app.data-source.api-url}")
    private String apiUrl;

    // DTO para mapear la respuesta de JSONPlaceholder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("userId")
        private Long userId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("body")
        private String body;

        // Getters y setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public String extractAndLoadData() {
        logger.info("Iniciando extracción de datos desde: {}", apiUrl);
        
        try {
            // 1. EXTRACT - Obtener datos de la API externa
            PostDto[] posts = restTemplate.getForObject(apiUrl, PostDto[].class);
            
            if (posts == null || posts.length == 0) {
                logger.warn("No se obtuvieron datos de la API");
                return "No se encontraron datos para procesar";
            }

            List<PostDto> postList = Arrays.asList(posts);
            logger.info("Extraídos {} registros de la API", postList.size());

            // 2. TRANSFORM & LOAD - Procesar y guardar cada registro
            int processedCount = 0;
            int skippedCount = 0;

            for (PostDto post : postList.subList(0, Math.min(10, postList.size()))) { // Solo primeros 10 para prueba
                try {
                    // Verificar si ya existe (evitar duplicados)
                    String externalId = "post-" + post.getId();
                    Optional<DataRecord> existing = dataRecordRepository.findByExternalId(externalId);
                    
                    if (existing.isPresent()) {
                        skippedCount++;
                        continue;
                    }

                    // TRANSFORM - Convertir DTO a Entity
                    DataRecord record = transformPostToDataRecord(post);
                    
                    // LOAD - Guardar en base de datos
                    dataRecordRepository.save(record);
                    processedCount++;
                    
                    logger.debug("Procesado registro: {}", record.getTitle());
                    
                } catch (Exception e) {
                    logger.error("Error procesando post ID {}: {}", post.getId(), e.getMessage());
                }
            }

            String result = String.format("ETL completado: %d procesados, %d omitidos", 
                                        processedCount, skippedCount);
            logger.info(result);
            return result;

        } catch (Exception e) {
            String errorMsg = "Error en proceso ETL: " + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }
    }

    private DataRecord transformPostToDataRecord(PostDto post) {
        DataRecord record = new DataRecord();
        
        record.setTitle(post.getTitle());
        record.setContent(post.getBody());
        record.setAuthorName("Usuario " + post.getUserId());
        record.setCategory("Blog Post");
        record.setExternalId("post-" + post.getId());
        record.setSourceUrl(apiUrl + "/" + post.getId());
        
        return record;
    }

    public long getTotalRecordsCount() {
        return dataRecordRepository.count();
    }

    public String getETLStatus() {
        long totalRecords = getTotalRecordsCount();
        return String.format("Total de registros en BD: %d", totalRecords);
    }
}