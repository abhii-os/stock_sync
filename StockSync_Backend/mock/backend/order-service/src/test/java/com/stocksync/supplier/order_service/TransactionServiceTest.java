package com.stocksync.supplier.order_service;

import com.stocksync.supplier.order_service.dto.ProductDTO;
import com.stocksync.supplier.order_service.dto.TransactionRequestDTO;
import com.stocksync.supplier.order_service.dto.TransactionStatusUpdateDTO;
import com.stocksync.supplier.order_service.exception.InsufficientStockException;
import com.stocksync.supplier.order_service.model.OrderStatus;
import com.stocksync.supplier.order_service.model.Transaction;
import com.stocksync.supplier.order_service.model.TransactionType;
import com.stocksync.supplier.order_service.repository.TransactionRepository;
import com.stocksync.supplier.order_service.service.ProductServiceClient;
import com.stocksync.supplier.order_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Setup: Use MockitoExtension to enable annotation processing for @Mock and @InjectMocks
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    // Dependencies to be mocked
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    // The service under test, with mocked dependencies injected
    @InjectMocks
    private TransactionService transactionService;

    // --- Mock Data Setup ---
    private final Long PRODUCT_ID = 101L;
    // Keeping BigInteger here as the service methods use it, but casting to Long for the setter
    private final BigInteger TRANSACTION_ID = BigInteger.valueOf(1L);
    private final Integer QUANTITY = 5;

    private TransactionRequestDTO sellRequest;
    private Transaction pendingSellTransaction;
    private Transaction completedSellTransaction;
    private Object dummyProductDetails; // Represents a successful (non-throwing) product details response

    @BeforeEach
    void setUp() {
        sellRequest = new TransactionRequestDTO(PRODUCT_ID, QUANTITY);
        // A dummy object to represent a successful product check
        dummyProductDetails = new Object();

        // Entity setup for a PENDING SELL transaction (as returned by save)
        pendingSellTransaction = new Transaction(PRODUCT_ID, QUANTITY, TransactionType.SELL);
        // FIX: Replaced setId(TRANSACTION_ID) with setOrderId(TRANSACTION_ID.longValue())
        pendingSellTransaction.setOrderId(TRANSACTION_ID.longValue());
        pendingSellTransaction.setStatus(OrderStatus.PENDING);

        // Entity setup for a COMPLETED SELL transaction
        completedSellTransaction = new Transaction(PRODUCT_ID, QUANTITY, TransactionType.SELL);
        // FIX: Replaced setId(TRANSACTION_ID) with setOrderId(TRANSACTION_ID.longValue())
        completedSellTransaction.setOrderId(TRANSACTION_ID.longValue());
        completedSellTransaction.setStatus(OrderStatus.COMPLETED);
    }

    // =========================================================================
    // 1. Test Case: Successful SELL Transaction Creation
    // =========================================================================
    @Test
    void createTransaction_Sell_Success() {
        // GIVEN:
        // 1. Product exists.
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn((Mono<ProductDTO>) Mono.just(dummyProductDetails));
        // 2. Stock is available.
        when(productServiceClient.checkStockAvailability(PRODUCT_ID, QUANTITY)).thenReturn(Mono.just(true));
        // 3. Repository saves the transaction successfully.
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingSellTransaction);

        // WHEN: Creating a new SELL transaction
        var response = transactionService.createTransaction(sellRequest, TransactionType.SELL);

        // THEN:
        assertNotNull(response);
        assertEquals(TRANSACTION_ID, response.orderId()); // Uses BigInteger TRANSACTION_ID for comparison
        assertEquals(OrderStatus.PENDING, response.status());
        verify(productServiceClient, times(1)).getProductDetails(PRODUCT_ID);
        verify(productServiceClient, times(1)).checkStockAvailability(PRODUCT_ID, QUANTITY);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // =========================================================================
    // 2. Test Case: Failed SELL Transaction due to Insufficient Stock
    // =========================================================================
    @Test
    void createTransaction_Sell_InsufficientStock_Failure() {
        // GIVEN:
        // 1. Product exists.
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.just(dummyProductDetails));
        // 2. Stock is NOT available (returns false).
        when(productServiceClient.checkStockAvailability(PRODUCT_ID, QUANTITY)).thenReturn(Mono.just(false));

        // WHEN / THEN: Expect an InsufficientStockException
        assertThrows(InsufficientStockException.class, () ->
                transactionService.createTransaction(sellRequest, TransactionType.SELL)
        );

        // Verify that the transaction was never saved
        verify(transactionRepository, never()).save(any());
        verify(productServiceClient, times(1)).checkStockAvailability(PRODUCT_ID, QUANTITY);
    }


    // =========================================================================
    // 3. Test Case: Status Update to COMPLETED (Successful Stock Update)
    // =========================================================================
    @Test
    void updateTransactionStatus_ToCompleted_StockUpdateSuccess() {
        // GIVEN:
        TransactionStatusUpdateDTO statusUpdate = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);
        Transaction pendingToCompleted = pendingSellTransaction; // Starts at PENDING

        // 1. Transaction is found in the repository.
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pendingToCompleted));
        // 2. Mock the stock update call to successfully complete (returns Mono.empty())
        when(productServiceClient.updateProductStock(pendingToCompleted)).thenReturn(Mono.empty());
        // 3. Repository saves the final COMPLETED state.
        when(transactionRepository.save(any(Transaction.class))).thenReturn(completedSellTransaction);

        // WHEN: Updating the status from PENDING to COMPLETED
        var response = transactionService.updateTransactionStatus(TRANSACTION_ID, statusUpdate);

        // THEN:
        assertEquals(OrderStatus.COMPLETED, response.status());
        // Verify that the stock update service was called once
        verify(productServiceClient, times(1)).updateProductStock(pendingToCompleted);
        // Two saves: 1st (internal): set status to COMPLETED; 2nd: after successful stock update
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    // =========================================================================
    // 4. Test Case: Status Reversion from COMPLETED (Successful Stock Reversal)
    // =========================================================================
    @Test
    void updateTransactionStatus_RevertFromCompleted_StockReversalSuccess() {
        // GIVEN:
        TransactionStatusUpdateDTO statusUpdate = new TransactionStatusUpdateDTO(OrderStatus.PENDING); // Reverting status
        Transaction completedToPending = completedSellTransaction; // Starts at COMPLETED

        // 1. Transaction is found in the repository (already COMPLETED).
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(completedToPending));
        // 2. Mock the stock reversal call to successfully complete.
        when(productServiceClient.reverseProductStockUpdate(completedToPending)).thenReturn(Mono.empty());
        // 3. Repository saves the final PENDING state.
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingSellTransaction);

        // WHEN: Updating the status from COMPLETED back to PENDING
        var response = transactionService.updateTransactionStatus(TRANSACTION_ID, statusUpdate);

        // THEN:
        assertEquals(OrderStatus.PENDING, response.status());
        // Verify the stock reversal method was called
        verify(productServiceClient, times(1)).reverseProductStockUpdate(completedToPending);
        // Only one save for the final status
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}