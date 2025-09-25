package com.pruebatecnica.agent.controller;

import com.pruebatecnica.agent.service.AIAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AIAgentController {

    @Autowired
    private AIAgentService aiAgentService;

    // POST /api/agent/query - Consulta en lenguaje natural (método recomendado)
    @PostMapping("/query")
    public ResponseEntity<AIAgentService.AIResponse> processQuery(@RequestBody QueryRequest request) {
        AIAgentService.AIResponse response = aiAgentService.processQuery(request.getQuery());
        return ResponseEntity.ok(response);
    }

    // GET /api/agent/ask - Consulta desde URL para pruebas rápidas
    @GetMapping("/ask")
    public ResponseEntity<AIAgentService.AIResponse> askQuestion(@RequestParam String q) {
        AIAgentService.AIResponse response = aiAgentService.processQuery(q);
        return ResponseEntity.ok(response);
    }

    // GET /api/agent/capabilities - Información sobre capacidades del agente
    @GetMapping("/capabilities")
    public ResponseEntity<AgentCapabilities> getCapabilities() {
        AgentCapabilities capabilities = new AgentCapabilities();
        return ResponseEntity.ok(capabilities);
    }

    // GET /api/agent/examples - Ejemplos de consultas
    @GetMapping("/examples")
    public ResponseEntity<String[]> getExamples() {
        String[] examples = {
            "¿Cuántos registros hay?",
            "Buscar posts sobre technology",
            "Mostrar los registros más recientes",
            "¿Qué autores tengo?",
            "Encuentra registros del Usuario 1",
            "¿Qué categorías hay disponibles?",
            "Listar posts de la categoría Blog Post"
        };
        return ResponseEntity.ok(examples);
    }

    // Clase para la petición de consulta
    public static class QueryRequest {
        private String query;

        public QueryRequest() {}

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    // Clase para las capacidades del agente
    public static class AgentCapabilities {
        public final String name = "Asistente de Datos - Prueba Técnica";
        public final String version = "1.0.0";
        public final String[] capabilities = {
            "Conteo de registros y estadísticas",
            "Búsqueda por palabras clave en títulos y contenido",
            "Filtrado por autores y categorías",
            "Mostrar registros más recientes",
            "Análisis básico de intenciones en lenguaje natural"
        };
        public final String[] supportedLanguages = {"Español", "English (basic)"};
        public final String[] intentTypes = {
            "count", "search", "recent", "author", "category", "general"
        };
        public final String[] endpoints = {
            "POST /api/agent/query - Consulta principal",
            "GET /api/agent/ask?q=consulta - Consulta rápida",
            "GET /api/agent/capabilities - Ver capacidades",
            "GET /api/agent/examples - Ejemplos de consultas"
        };
    }
}