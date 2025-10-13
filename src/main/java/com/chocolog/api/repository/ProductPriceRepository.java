package com.chocolog.api.repository;

import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.ProductPrice;
import com.chocolog.api.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    Optional<ProductPrice> findByFlavorAndSize(Flavor flavor, Size size);

}