package com.pruebatecnica.agent.service;

import com.pruebatecnica.entity.DataRecord;
import com.pruebatecnica.repository.DataRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIAgentService {

    private static final Logger logger = LoggerFactory.getLogger(AIAgentService.class);

    @Autowired
    private DataRecordRepository dataRecordRepository;

    public AIResponse processQuery(String query) {
        logger.info("Procesando consulta: {}", query);
        
        String normalizedQuery = query.toLowerCase().trim();
        
        try {
            // Análisis de intención basado en palabras clave
            if (containsKeywords(normalizedQuery, "cuántos", "cantidad", "total", "count")) {
                return handleCountQuery(normalizedQuery);
            }
            
            if (containsKeywords(normalizedQuery, "buscar", "encuentra", "mostrar", "listar")) {
                return handleSearchQuery(normalizedQuery);
            }
            
            if (containsKeywords(normalizedQuery, "último", "reciente", "nuevo")) {
                return handleRecentQuery(normalizedQuery);
            }
            
            if (containsKeywords(normalizedQuery, "autor", "usuario", "who", "quién")) {
                return handleAuthorQuery(normalizedQuery);
            }
            
            if (containsKeywords(normalizedQuery, "categoría", "tipo", "category")) {
                return handleCategoryQuery(normalizedQuery);
            }
            
            // Búsqueda general por defecto
            return handleGeneralSearch(normalizedQuery);
            
        } catch (Exception e) {
            logger.error("Error procesando consulta: {}", e.getMessage());
            return new AIResponse(
                "Lo siento, ocurrió un error procesando tu consulta: " + e.getMessage(),
                null,
                "error"
            );
        }
    }

    private boolean containsKeywords(String query, String... keywords) {
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private AIResponse handleCountQuery(String query) {
        long total = dataRecordRepository.count();
        
        if (containsKeywords(query, "usuario", "autor")) {
            List<Object[]> authorStats = dataRecordRepository.countByCategory();
            return new AIResponse(
                String.format("Hay %d registros en total. Aquí tienes las estadísticas por categoría:", total),
                null,
                "count"
            );
        }
        
        return new AIResponse(
            String.format("Actualmente tengo %d registros en la base de datos.", total),
            null,
            "count"
        );
    }

    private AIResponse handleSearchQuery(String query) {
        // Extraer palabras clave de la consulta
        String searchTerm = extractSearchTerm(query);
        
        if (searchTerm.isEmpty()) {
            return handleRecentQuery(query);
        }
        
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<DataRecord> results = dataRecordRepository.findByKeyword(searchTerm, pageable);
        
        if (results.isEmpty()) {
            return new AIResponse(
                String.format("No encontré registros que contengan '%s'. ¿Podrías intentar con otros términos?", searchTerm),
                null,
                "search_empty"
            );
        }
        
        return new AIResponse(
            String.format("Encontré %d registros relacionados con '%s':", results.getTotalElements(), searchTerm),
            results.getContent(),
            "search_results"
        );
    }

    private AIResponse handleRecentQuery(String query) {
        List<DataRecord> recent = dataRecordRepository.findTop10ByOrderByCreatedAtDesc();
        
        return new AIResponse(
            "Aquí están los registros más recientes:",
            recent,
            "recent"
        );
    }

    private AIResponse handleAuthorQuery(String query) {
        // Buscar por autor específico si se menciona un nombre
        String authorName = extractAuthorName(query);
        
        if (!authorName.isEmpty()) {
            List<DataRecord> results = dataRecordRepository.findByAuthorNameContainingIgnoreCase(authorName);
            
            if (results.isEmpty()) {
                return new AIResponse(
                    String.format("No encontré registros del autor '%s'.", authorName),
                    null,
                    "author_not_found"
                );
            }
            
            return new AIResponse(
                String.format("Encontré %d registros del autor '%s':", results.size(), authorName),
                results.stream().limit(5).collect(Collectors.toList()),
                "author_results"
            );
        }
        
        // Mostrar estadísticas de autores
        return new AIResponse(
            "Estos son los autores que tengo en la base de datos. ¿Sobre cuál te gustaría saber más?",
            dataRecordRepository.findTop10ByOrderByCreatedAtDesc(),
            "authors_overview"
        );
    }

    private AIResponse handleCategoryQuery(String query) {
        List<DataRecord> blogPosts = dataRecordRepository.findByCategory("Blog Post");
        List<DataRecord> testPosts = dataRecordRepository.findByCategory("Test");
        
        return new AIResponse(
            String.format("Tengo registros en las siguientes categorías: Blog Post (%d), Test (%d)", 
                         blogPosts.size(), testPosts.size()),
            blogPosts.stream().limit(3).collect(Collectors.toList()),
            "category_overview"
        );
    }

    private AIResponse handleGeneralSearch(String query) {
        // Buscar en título y contenido
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<DataRecord> results = dataRecordRepository.findByKeyword(query, pageable);
        
        if (results.isEmpty()) {
            return new AIResponse(
                "No encontré resultados específicos para tu consulta. Aquí tienes algunos registros recientes:",
                dataRecordRepository.findTop10ByOrderByCreatedAtDesc().stream().limit(3).collect(Collectors.toList()),
                "general_fallback"
            );
        }
        
        return new AIResponse(
            String.format("Esto es lo que encontré relacionado con tu consulta (%d resultados):", results.getTotalElements()),
            results.getContent(),
            "general_results"
        );
    }

    private String extractSearchTerm(String query) {
        // Remover palabras comunes y extraer término de búsqueda
        String[] commonWords = {"buscar", "encuentra", "mostrar", "listar", "sobre", "acerca", "de", "el", "la", "los", "las", "un", "una"};
        String[] words = query.split("\\s+");
        
        for (String word : words) {
            boolean isCommon = false;
            for (String common : commonWords) {
                if (word.equalsIgnoreCase(common)) {
                    isCommon = true;
                    break;
                }
            }
            if (!isCommon && word.length() > 2) {
                return word;
            }
        }
        return "";
    }

    private String extractAuthorName(String query) {
        // Buscar patrones como "usuario 1", "user 1", etc.
        if (query.contains("usuario") || query.contains("user")) {
            String[] words = query.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].toLowerCase().contains("usuario") || words[i].toLowerCase().contains("user")) {
                    return "Usuario " + words[i + 1];
                }
            }
        }
        return "";
    }

    // Clase interna para la respuesta del agente
    public static class AIResponse {
        private String message;
        private List<DataRecord> data;
        private String intentType;

        public AIResponse(String message, List<DataRecord> data, String intentType) {
            this.message = message;
            this.data = data;
            this.intentType = intentType;
        }

        // Getters
        public String getMessage() { return message; }
        public List<DataRecord> getData() { return data; }
        public String getIntentType() { return intentType; }
    }
}