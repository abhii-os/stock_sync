package com.stocksync.supplier.order_service.service;

import com.stocksync.supplier.order_service.dto.ProductDTO;
import com.stocksync.supplier.order_service.dto.TransactionRequestDTO;
import com.stocksync.supplier.order_service.dto.TransactionResponseDTO;
import com.stocksync.supplier.order_service.dto.TransactionStatusUpdateDTO;
//import com.stocksync.order_service.exception.*;
import com.stocksync.supplier.order_service.exception.*;
import com.stocksync.supplier.order_service.exception.*;
import com.stocksync.supplier.order_service.model.OrderStatus;
import com.stocksync.supplier.order_service.model.Transaction;
import com.stocksync.supplier.order_service.model.TransactionType;
import com.stocksync.supplier.order_service.repository.TransactionRepository;
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

        // 1. Fetch product details and check stock
        try {
            // STEP 1A: Fetch the product details (including price)
            productDetails = productServiceClient.getProductDetails(productId).block();

            // Basic null check for product not found
            if (productDetails == null || productDetails.getPrice() == null) {
                // Modified check to include price being null
                throw new ProductNotFoundException("Product ID not found or price is missing for ID: " + productId);
            }

            // STEP 1B: Validate stock for SELL transactions
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
            // Catch any communication or generic product service error
            throw new InvalidTransactionException("Invalid Updation due to current stock Quantity" + e.getMessage());
        }

        // --- CORE CHANGE: Correctly extract and convert price ---
        // 1. Use Lombok-generated getter getPrice()
        // 2. Convert BigDecimal to double (loss of precision is possible, but matching your Transaction model)
        double pricePerUnit = productDetails.getPrice().doubleValue();
        // --------------------------------------------------------

        // 2. Create and save the transaction with full financial details
        Transaction transaction = new Transaction(
                productId,
                requestDTO.quantity(),
                pricePerUnit, // <-- CORRECTLY PASSED double value
                type,
                userId
        );

        transaction = transactionRepository.save(transaction);
        // The @PrePersist/onCreate hook sets the status, but setting here is fine too.
        transaction.setStatus(OrderStatus.PENDING);

        return TransactionResponseDTO.fromEntity(transaction);
    }


    /**
     * Retrieves a single transaction by its ID.
     */
    public TransactionResponseDTO findTransactionById(BigInteger id) {
        // ... (No change required here)
        return transactionRepository.findById(id)
                .map(TransactionResponseDTO::fromEntity)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    /**
     * Retrieves all transactions.
     */
    public List<TransactionResponseDTO> findAllTransactions() {
        // ... (No change required here)
        return transactionRepository.findAll().stream()
                .map(TransactionResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Updates the status of a specific transaction.
     * * @param updatingUserId The ID of the user/service performing the update (NEW PARAMETER)
     */
    public TransactionResponseDTO updateTransactionStatus(
            BigInteger id,
            TransactionStatusUpdateDTO statusUpdateDTO,
            String updatingUserId // <-- ADDED UPDATING USER ID PARAMETER
    ) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        OrderStatus oldStatus = transaction.getStatus(); // **Capture the old status**
        OrderStatus newStatus = statusUpdateDTO.newStatus();
        if (newStatus != oldStatus) {
            transaction.setStatus(newStatus);

            // --- 1. Logic for NEW Status: COMPLETED (Stock Update) ---
            if (newStatus == OrderStatus.COMPLETED) {
                // ONLY execute this logic if the transaction was NOT already COMPLETED
                if (oldStatus != OrderStatus.COMPLETED) {
                    // Logic to decrease/increase stock for the first time
                    try {
                        // This service call should handle the stock adjustment (e.g., -quantity for SELL, +quantity for PURCHASE)
                        productServiceClient.updateProductStock(transaction).block();
                        // Set status to COMPLETED and save
                        transaction = transactionRepository.save(transaction);
                    } catch (Exception e) {
                        // Rollback service call failure status
                        transaction.setStatus(OrderStatus.FAILED);
                        transaction = transactionRepository.save(transaction);
                        throw new StockServiceException("Failed to update product stock: " + e.getMessage());
                    }
                }
            }

            // --- 2. Logic for REVERTED Status: From COMPLETED to NON-COMPLETED (Stock Reversal) ---
            else if (oldStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
                // This handles the rollback: when status is reverted (e.g., COMPLETED -> PENDING)
                System.out.println("Reverting stock change for transaction ID: " + id);

                try {
                    // You need a new method in ProductServiceClient to perform the stock reversal.
                    productServiceClient.reverseProductStockUpdate(transaction).block();

                    // Stock reversal successful, save the new non-completed status (e.g., PENDING)
                    transaction = transactionRepository.save(transaction);
                } catch (Exception e) {
                    log.error("CRITICAL ERROR: Failed to reverse product stock: {}", e.getMessage());
                    transaction = transactionRepository.save(transaction);
                    throw new InvalidTransactionException("Failed to reverse product stock: " + e.getMessage());
                }
            }

            // --- 3. Save for simple status changes (e.g., PENDING -> SHIPPED, etc.) ---
            else {
                // For any other non-stock-affecting status change
                transaction = transactionRepository.save(transaction);
            }
        }

        return TransactionResponseDTO.fromEntity(transaction);
    }
}