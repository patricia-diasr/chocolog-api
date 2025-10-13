package com.chocolog.api.service;

import com.chocolog.api.dto.request.FlavorRequestDTO;
import com.chocolog.api.dto.request.FlavorPatchRequestDTO;
import com.chocolog.api.dto.request.PriceRequestDTO;
import com.chocolog.api.dto.response.FlavorResponseDTO;
import com.chocolog.api.dto.response.FlavorSizeResponseDTO;
import com.chocolog.api.mapper.FlavorMapper;
import com.chocolog.api.model.Flavor;
import com.chocolog.api.model.ProductPrice;
import com.chocolog.api.model.Size;
import com.chocolog.api.model.Stock;
import com.chocolog.api.repository.FlavorRepository;
import com.chocolog.api.repository.ProductPriceRepository;
import com.chocolog.api.repository.SizeRepository;
import com.chocolog.api.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FlavorService {

    private final SizeRepository sizeRepository;
    private final ProductPriceRepository productPriceRepository;
    private final StockRepository stockRepository;
    private final FlavorRepository flavorRepository;
    private final FlavorMapper flavorMapper;

    public List<FlavorResponseDTO> findAll() {
        return flavorRepository.findAll().stream()
                .map(this::buildFlavorResponseDTO)
                .toList();
    }

    public FlavorResponseDTO findById(Long id) {
        Flavor flavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));
        return buildFlavorResponseDTO(flavor);
    }

    @Transactional
    public FlavorResponseDTO save(FlavorRequestDTO flavorDTO) {
        flavorRepository.findByName(flavorDTO.getName()).ifPresent(e -> {
            throw new IllegalArgumentException("The name '" + flavorDTO.getName() + "' is already in use.");
        });

        Flavor flavor = flavorMapper.toEntity(flavorDTO);
        Flavor savedFlavor = flavorRepository.save(flavor);

        if (flavorDTO.getPrices() != null) {
            for (PriceRequestDTO priceDTO : flavorDTO.getPrices()) {
                Size size = sizeRepository.findById(priceDTO.getSizeId())
                        .orElseThrow(() -> new EntityNotFoundException("Size not found for id: " + priceDTO.getSizeId()));

                ProductPrice productPrice = ProductPrice.builder()
                        .flavor(savedFlavor)
                        .size(size)
                        .salePrice(priceDTO.getSalePrice())
                        .costPrice(priceDTO.getCostPrice())
                        .build();
                productPriceRepository.save(productPrice);

                Stock stock = Stock.builder()
                        .flavor(savedFlavor)
                        .size(size)
                        .totalQuantity(0)
                        .remainingQuantity(0)
                        .build();
                stockRepository.save(stock);
            }
        }

        return findById(savedFlavor.getId());
    }

    @Transactional
    public FlavorResponseDTO update(Long id, FlavorPatchRequestDTO flavorDTO) {
        Flavor existingFlavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));

        if (flavorDTO.getName() != null && !flavorDTO.getName().equalsIgnoreCase(existingFlavor.getName())) {
            flavorRepository.findByName(flavorDTO.getName()).ifPresent(e -> {
                throw new IllegalArgumentException("The name '" + flavorDTO.getName() + "' is already in use.");
            });
            existingFlavor.setName(flavorDTO.getName());
        }

        if (flavorDTO.getPrices() != null) {
            for (PriceRequestDTO priceDTO : flavorDTO.getPrices()) {
                Size size = sizeRepository.findById(priceDTO.getSizeId())
                        .orElseThrow(() -> new EntityNotFoundException("Size not found for id: " + priceDTO.getSizeId()));

                ProductPrice productPrice = productPriceRepository.findByFlavorAndSize(existingFlavor, size)
                        .orElseGet(() -> {
                            stockRepository.findByFlavorAndSize(existingFlavor, size)
                                    .orElseGet(() -> stockRepository.save(Stock.builder()
                                            .flavor(existingFlavor).size(size)
                                            .totalQuantity(0).remainingQuantity(0).build()));
                            
                            return ProductPrice.builder().flavor(existingFlavor).size(size).build();
                        });

                if (priceDTO.getSalePrice() != null) {
                    productPrice.setSalePrice(priceDTO.getSalePrice());
                }
                if (priceDTO.getCostPrice() != null) {
                    productPrice.setCostPrice(priceDTO.getCostPrice());
                }
                productPriceRepository.save(productPrice);
            }
        }

        return findById(existingFlavor.getId());
    }

    public void deleteById(Long id) {
        Flavor flavor = flavorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found for id: " + id));
        flavorRepository.delete(flavor);
    }

    private FlavorResponseDTO buildFlavorResponseDTO(Flavor flavor) {
        FlavorResponseDTO responseDTO = flavorMapper.toResponseDTO(flavor);
        List<Size> allSizes = sizeRepository.findAll();

        List<FlavorSizeResponseDTO> sizeInfoList = allSizes.stream()
                .map(size -> {
                    ProductPrice price = productPriceRepository.findByFlavorAndSize(flavor, size).orElse(null);
                    Stock stock = stockRepository.findByFlavorAndSize(flavor, size).orElse(null);

                    return new FlavorSizeResponseDTO(
                            size.getId(),
                            size.getName(),
                            price != null ? price.getSalePrice() : BigDecimal.ZERO,
                            price != null ? price.getCostPrice() : BigDecimal.ZERO,
                            stock != null ? stock.getTotalQuantity() : 0,
                            stock != null ? stock.getRemainingQuantity() : 0
                    );
                }).toList();

        responseDTO.setSizes(sizeInfoList);
        return responseDTO;
    }
}