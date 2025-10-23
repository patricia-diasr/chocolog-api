package com.chocolog.api.service;

import com.chocolog.api.dto.request.StockRecordRequestDTO;
import com.chocolog.api.dto.response.StockRecordResponseDTO;
import com.chocolog.api.mapper.StockRecordMapper;
import com.chocolog.api.model.StockRecord;
import com.chocolog.api.model.StockMovement;
import com.chocolog.api.model.Stock;
import com.chocolog.api.model.Size;
import com.chocolog.api.model.Flavor;
import com.chocolog.api.repository.StockRecordRepository;
import com.chocolog.api.repository.StockRepository;
import com.chocolog.api.repository.SizeRepository;
import com.chocolog.api.repository.FlavorRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; 
import java.util.List;


@Service
@RequiredArgsConstructor
public class StockRecordService {

    private final StockRepository stockRepository;
    private final StockRecordRepository stockRecordRepository;
    private final FlavorRepository flavorRepository;
    private final SizeRepository sizeRepository;
    private final StockRecordMapper stockRecordMapper;

    public List<StockRecordResponseDTO> findAll() {
        return stockRecordRepository.findAll().stream()
                .map(stockRecordMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public StockRecordResponseDTO save(StockRecordRequestDTO requestDTO) {
        Flavor flavor = flavorRepository.findById(requestDTO.getFlavorId())
                .orElseThrow(() -> new EntityNotFoundException("Flavor not found with id: " + requestDTO.getFlavorId()));

        Size size = sizeRepository.findById(requestDTO.getSizeId())
                .orElseThrow(() -> new EntityNotFoundException("Size not found with id: " + requestDTO.getSizeId()));

        Stock stock = stockRepository.findByFlavorAndSize(flavor, size)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setFlavor(flavor);
                    newStock.setSize(size);
                    newStock.setTotalQuantity(0);
                    newStock.setRemainingQuantity(0);
                    return newStock;
                });

        StockMovement movementType = StockMovement.valueOf(requestDTO.getMovementType());
        Integer quantityChange = requestDTO.getQuantity(); 

        if (movementType == StockMovement.INBOUND) {
            stock.setTotalQuantity(stock.getTotalQuantity() + quantityChange);
            stock.setRemainingQuantity(stock.getRemainingQuantity() + quantityChange); 
        } else {
            if (stock.getTotalQuantity() < quantityChange) {
                throw new IllegalArgumentException("Insufficient stock for flavor: " + flavor.getName() + ", size: " + size.getName());
            }
            stock.setTotalQuantity(stock.getTotalQuantity() - quantityChange);
            stock.setRemainingQuantity(stock.getRemainingQuantity() - quantityChange); 
        }
        
        stockRepository.save(stock);

        StockRecord stockRecord = stockRecordMapper.toEntity(requestDTO);
        stockRecord.setFlavor(flavor);
        stockRecord.setSize(size);
        stockRecord.setMovementType(movementType); 

        LocalDateTime transactionDate = LocalDateTime.now();
        stockRecord.setProductionDate(transactionDate);

        if (movementType == StockMovement.INBOUND) {
            LocalDateTime expirationDate = transactionDate.plusDays(30);
            stockRecord.setExpirationDate(expirationDate);
        } else {
            stockRecord.setExpirationDate(null); 
        }
        
        StockRecord savedStockRecord = stockRecordRepository.save(stockRecord);
        return stockRecordMapper.toResponseDTO(savedStockRecord);
    }
}