package com.stocksync.order_service.service;

import com.stocksync.order_service.dto.ProductDTO;
import com.stocksync.order_service.dto.TransactionRequestDTO;
import com.stocksync.order_service.dto.TransactionResponseDTO;
import com.stocksync.order_service.dto.TransactionStatusUpdateDTO;
import com.stocksync.order_service.exception.*;
import com.stocksync.order_service.model.OrderStatus;
import com.stocksync.order_service.model.Transaction;
import com.stocksync.order_service.model.TransactionType;
import com.stocksync.order_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final ProductServiceClient productServiceClient;

    public TransactionService(
            TransactionRepository transactionRepository,
            ProductServiceClient productServiceClient) {
        this.transactionRepository = transactionRepository;
        this.productServiceClient = productServiceClient;
    }

    public TransactionResponseDTO createTransaction(
            TransactionRequestDTO requestDTO,
            TransactionType type,
            String userId
    ) {
        Long productId = requestDTO.productId();
        ProductDTO productDetails;

        try {
            productDetails = productServiceClient.getProductDetails(productId).block();

            if (productDetails == null || productDetails.getPrice() == null) {
                throw new ProductNotFoundException("Product ID not found or price is missing for ID: " + productId);
            }

            if (type == TransactionType.SELL) {
                Boolean isStockAvailable = productServiceClient.checkStockAvailability(
                        productId, requestDTO.quantity()
                ).block();

                if (Boolean.FALSE.equals(isStockAvailable)) {
                    throw new InsufficientStockException(
                            "Insufficient stock for product ID: " + productId
                    );
                }
            }
        } catch (InsufficientStockException | ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTransactionException("Invalid Updation due to current stock Quantity" + e.getMessage());
        }

        double pricePerUnit = productDetails.getPrice().doubleValue();

        Transaction transaction = new Transaction(
                productId,
                requestDTO.quantity(),
                pricePerUnit,
                type,
                userId
        );

        transaction = transactionRepository.save(transaction);

        return TransactionResponseDTO.fromEntity(transaction);
    }

    public TransactionResponseDTO findTransactionById(BigInteger id) {
        return transactionRepository.findById(id)
                .map(TransactionResponseDTO::fromEntity)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public List<TransactionResponseDTO> findAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponseDTO::fromEntity)
                .toList();
    }

    public TransactionResponseDTO updateTransactionStatus(
            BigInteger id,
            TransactionStatusUpdateDTO statusUpdateDTO,
            String updatingUserId
    ) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        OrderStatus oldStatus = transaction.getStatus();
        OrderStatus newStatus = statusUpdateDTO.newStatus();
        if (newStatus != oldStatus) {
            transaction.setStatus(newStatus);

            if (newStatus == OrderStatus.COMPLETED) {
                if (oldStatus != OrderStatus.COMPLETED) {
                    try {
                        productServiceClient.updateProductStock(transaction).block();
                        transaction = transactionRepository.save(transaction);
                    } catch (Exception e) {
                        transaction.setStatus(OrderStatus.FAILED);
                        transaction = transactionRepository.save(transaction);
                        throw new StockServiceException("Failed to update product stock: " + e.getMessage());
                    }
                }
            } else if (oldStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
                log.info("Reverting stock change for transaction ID: {}", id);

                try {
                    productServiceClient.reverseProductStockUpdate(transaction).block();
                    transaction = transactionRepository.save(transaction);
                } catch (Exception e) {
                    log.error("CRITICAL ERROR: Failed to reverse product stock: {}", e.getMessage());
                    transaction = transactionRepository.save(transaction);
                    throw new InvalidTransactionException("Failed to reverse product stock: " + e.getMessage());
                }
            } else {
                transaction = transactionRepository.save(transaction);
            }
        }

        return TransactionResponseDTO.fromEntity(transaction);
    }
}
