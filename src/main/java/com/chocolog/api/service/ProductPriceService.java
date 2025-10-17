package com.chocolog.api.service;

import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.ProductPrice;
import com.chocolog.api.model.Size;
import com.chocolog.api.repository.ProductPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ProductPriceService {

    private final ProductPriceRepository productPriceRepository;

    public BigDecimal calculateUnitPrice(Size size, Flavor flavor1, Flavor flavor2) {
        ProductPrice price1 = findPriceOrFail(flavor1, size);

        if (flavor2 != null) {
            ProductPrice price2 = findPriceOrFail(flavor2, size);
            return price1.getSalePrice()
                    .add(price2.getSalePrice())
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }

        return price1.getSalePrice();
    }

    private ProductPrice findPriceOrFail(Flavor flavor, Size size) {
        return productPriceRepository.findByFlavorAndSize(flavor, size)
                .orElseThrow(() -> new EntityNotFoundException("Price not found for flavor id: " + flavor.getId() + " and size id: " + size.getId()));
    }
}