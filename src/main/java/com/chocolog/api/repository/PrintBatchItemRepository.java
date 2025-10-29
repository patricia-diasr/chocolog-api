package com.chocolog.api.repository;

import com.chocolog.api.model.PrintBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PrintBatchItemRepository extends JpaRepository<PrintBatchItem, Long> {

    @Query("""
        SELECT pbi.orderItem.id 
        FROM PrintBatchItem pbi 
        WHERE pbi.orderItem.id IN :orderItemIds
    """)
    Set<Long> findPrintedOrderItemIds(@Param("orderItemIds") Set<Long> orderItemIds);

}