package com.pruebatecnica.repository;

import com.pruebatecnica.entity.DataRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    // Buscar por external_id (útil para evitar duplicados en ETL)
    Optional<DataRecord> findByExternalId(String externalId);

    // Buscar por categoría
    List<DataRecord> findByCategory(String category);

    // Búsqueda por palabras clave en título o contenido
    @Query("SELECT d FROM DataRecord d WHERE " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<DataRecord> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Buscar por autor
    List<DataRecord> findByAuthorNameContainingIgnoreCase(String authorName);

    // Buscar por rango de fechas
    List<DataRecord> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Búsqueda avanzada combinando múltiples criterios
    @Query("SELECT d FROM DataRecord d WHERE " +
           "(:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:author IS NULL OR LOWER(d.authorName) LIKE LOWER(CONCAT('%', :author, '%')))")
    Page<DataRecord> findWithFilters(@Param("keyword") String keyword, 
                                   @Param("category") String category, 
                                   @Param("author") String author, 
                                   Pageable pageable);

    // Contar registros por categoría
    @Query("SELECT d.category, COUNT(d) FROM DataRecord d GROUP BY d.category")
    List<Object[]> countByCategory();

    // Obtener registros más recientes
    List<DataRecord> findTop10ByOrderByCreatedAtDesc();
}