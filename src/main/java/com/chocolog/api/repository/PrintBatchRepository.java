package com.chocolog.api.repository;

import com.chocolog.api.model.PrintBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrintBatchRepository extends JpaRepository<PrintBatch, Long> {

    @Query("SELECT b FROM PrintBatch b JOIN FETCH b.printedBy")
    List<PrintBatch> findAllWithEmployee();

    @Query("SELECT b FROM PrintBatch b JOIN FETCH b.printedBy LEFT JOIN FETCH b.items WHERE b.id = :id")
    Optional<PrintBatch> findByIdWithEmployeeAndItems(@Param("id") Long id);

    @Query("SELECT b FROM PrintBatch b " +
            "JOIN FETCH b.printedBy " +
            "LEFT JOIN FETCH b.items i " +
            "LEFT JOIN FETCH i.orderItem oi " +
            "LEFT JOIN FETCH oi.order o " +
            "LEFT JOIN FETCH o.customer c " +
            "LEFT JOIN FETCH oi.size s " +
            "LEFT JOIN FETCH oi.flavor1 f1 " +
            "LEFT JOIN FETCH oi.flavor2 f2 " +
            "WHERE b.id = :id")
    Optional<PrintBatch> findFullyLoadedBatchById(@Param("id") Long id);
}