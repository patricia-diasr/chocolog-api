package com.chocolog.api.repository;

import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.Stock;
import com.chocolog.api.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByFlavorAndSize(Flavor flavor, Size size);

}