package com.stocksync.order_service;

import com.stocksync.order_service.dto.*;
import com.stocksync.order_service.exception.*;
import com.stocksync.order_service.model.OrderStatus;
import com.stocksync.order_service.model.Transaction;
import com.stocksync.order_service.model.TransactionType;
import com.stocksync.order_service.repository.TransactionRepository;
import com.stocksync.order_service.service.ProductServiceClient;
import com.stocksync.order_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Use MockitoExtension to enable mocking annotations
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    // Mock the dependencies
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    // Inject the mocked dependencies into the service instance being tested
    @InjectMocks
    private TransactionService transactionService;

    // --- Common Test Data ---
    private final Long PRODUCT_ID = 101L;
    private final BigInteger TRANSACTION_ID = BigInteger.valueOf(999);
    private final String USER_ID = "testUser123";

    private TransactionRequestDTO sellRequest;
    private TransactionRequestDTO purchaseRequest;
    private ProductDTO productDTO;
    private Transaction pendingTransaction;

    @BeforeEach
    void setUp() {
        // Setup DTOs
        sellRequest = new TransactionRequestDTO(PRODUCT_ID, 5);
        purchaseRequest = new TransactionRequestDTO(PRODUCT_ID, 10);

        // Setup Product Details (Price should be BigDecimal for DTO)
        productDTO = new ProductDTO(PRODUCT_ID, "Test Product", "Desc", BigDecimal.valueOf(50.00), 100);

        // Setup a base Transaction entity
        pendingTransaction = new Transaction(
                PRODUCT_ID, 5, 50.00, TransactionType.SELL, USER_ID
        );
        pendingTransaction.setOrderId(TRANSACTION_ID);
        pendingTransaction.setStatus(OrderStatus.PENDING);
    }

    // --- Test Cases for createTransaction (SELL) ---

    @Test
    void createTransaction_Sell_Success() {
        // GIVEN: Mock successful calls
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.just(productDTO));
        when(productServiceClient.checkStockAvailability(PRODUCT_ID, sellRequest.quantity()))
                .thenReturn(Mono.just(true));
        
        // Mock the repository save to return the same entity (simulating successful persistence)
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingTransaction);

        // WHEN
        TransactionResponseDTO response = transactionService.createTransaction(
                sellRequest, TransactionType.SELL, USER_ID
        );

        // THEN
        assertNotNull(response);
        assertEquals(TRANSACTION_ID, response.orderId());
        assertEquals(50.00, response.pricePerUnit());
        assertEquals(OrderStatus.PENDING, response.status());

        // Verify the interactions
        verify(productServiceClient, times(1)).getProductDetails(PRODUCT_ID);
        verify(productServiceClient, times(1)).checkStockAvailability(PRODUCT_ID, 5);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_Sell_InsufficientStock_ThrowsException() {
        // GIVEN
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.just(productDTO));
        when(productServiceClient.checkStockAvailability(PRODUCT_ID, sellRequest.quantity()))
                .thenReturn(Mono.just(false)); // Stock check fails

        // WHEN / THEN
        assertThrows(InsufficientStockException.class, () -> {
            transactionService.createTransaction(sellRequest, TransactionType.SELL, USER_ID);
        });

        verify(transactionRepository, never()).save(any());
    }
    
    // --- Test Cases for createTransaction (PURCHASE) ---

    @Test
    void createTransaction_Purchase_Success() {
        // GIVEN: Purchase skips the stock check (only ProductDetails is called)
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.just(productDTO));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingTransaction);

        // WHEN
        TransactionResponseDTO response = transactionService.createTransaction(
                purchaseRequest, TransactionType.PURCHASE, USER_ID
        );

        // THEN
        assertNotNull(response);
        assertEquals(TransactionType.PURCHASE, response.type()); // Ensure type is correct
        
        // Verify only getProductDetails was called, not checkStockAvailability
        verify(productServiceClient, times(1)).getProductDetails(PRODUCT_ID);
        verify(productServiceClient, never()).checkStockAvailability(any(), anyInt());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // --- Test Cases for createTransaction (Error Scenarios) ---

    @Test
    void createTransaction_ProductNotFound_ThrowsException() {
        // GIVEN: Mock product details fetching failure
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.justOrEmpty(Optional.empty()));

        // WHEN / THEN
        assertThrows(ProductNotFoundException.class, () -> {
            transactionService.createTransaction(sellRequest, TransactionType.SELL, USER_ID);
        });
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_ProductServiceError_ThrowsInvalidTransactionException() {
        // GIVEN: Mock product service client throwing a generic Exception
        when(productServiceClient.getProductDetails(PRODUCT_ID)).thenReturn(Mono.error(new RuntimeException("API Down")));

        // WHEN / THEN
        assertThrows(InvalidTransactionException.class, () -> {
            transactionService.createTransaction(sellRequest, TransactionType.SELL, USER_ID);
        });
        verify(transactionRepository, never()).save(any());
    }
    
    // --- Test Cases for findTransactionById ---
    
    @Test
    void findTransactionById_Found_ReturnsDTO() {
        // GIVEN
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pendingTransaction));

        // WHEN
        TransactionResponseDTO result = transactionService.findTransactionById(TRANSACTION_ID);

        // THEN
        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.orderId());
    }

    @Test
    void findTransactionById_NotFound_ThrowsException() {
        // GIVEN
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.findTransactionById(TRANSACTION_ID);
        });
    }

    // --- Test Cases for updateTransactionStatus (COMPLETED Logic) ---

    @Test
    void updateTransactionStatus_ToCompleted_StockUpdateSuccess() {
        // GIVEN: Setup the transaction to be PENDING
        Transaction pending = pendingTransaction;
        Transaction completed = new Transaction(pending.getProductId(), pending.getQuantity(), pending.getPricePerUnit(), pending.getType(), pending.getUserId());
        completed.setId(TRANSACTION_ID);
        completed.setStatus(OrderStatus.COMPLETED);

        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);
        
        // Mock: Find PENDING transaction, Stock update success, Save COMPLETED transaction
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pending));
        when(productServiceClient.updateProductStock(any(Transaction.class))).thenReturn(Mono.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(completed);

        // WHEN
        TransactionResponseDTO response = transactionService.updateTransactionStatus(
                TRANSACTION_ID, updateDTO, USER_ID
        );

        // THEN
        assertEquals(OrderStatus.COMPLETED, response.status());
        // Verify stock update was called
        verify(productServiceClient, times(1)).updateProductStock(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void updateTransactionStatus_ToCompleted_StockUpdateFails_RollsBackToFAILED() {
        // GIVEN: Setup the transaction to be PENDING
        Transaction pending = pendingTransaction;
        Transaction failed = new Transaction(pending.getProductId(), pending.getQuantity(), pending.getPricePerUnit(), pending.getType(), pending.getUserId());
        failed.setId(TRANSACTION_ID);
        failed.setStatus(OrderStatus.FAILED); // This is the final saved status

        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);

        // Mock: Find PENDING transaction, Stock update FAILS, Save FAILED transaction
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pending));
        when(productServiceClient.updateProductStock(any(Transaction.class))).thenReturn(Mono.error(new RuntimeException("Stock API Error")));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(failed); // Mock save call

        // WHEN / THEN
        assertThrows(StockServiceException.class, () -> {
            transactionService.updateTransactionStatus(TRANSACTION_ID, updateDTO, USER_ID);
        });

        // Verify: updateProductStock called, save called twice (once inside catch block)
        verify(productServiceClient, times(1)).updateProductStock(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertEquals(OrderStatus.FAILED, pending.getStatus()); // Verify status entity was updated before saving
    }

    @Test
    void updateTransactionStatus_FromCompletedToPending_StockReversalSuccess() {
        // GIVEN: Setup the transaction to be COMPLETED
        Transaction completed = new Transaction(pendingTransaction.getProductId(), pendingTransaction.getQuantity(), pendingTransaction.getPricePerUnit(), pendingTransaction.getType(), pendingTransaction.getUserId());
        completed.setId(TRANSACTION_ID);
        completed.setStatus(OrderStatus.COMPLETED); // Start as COMPLETED

        Transaction pendingAfterReversal = new Transaction(completed.getProductId(), completed.getQuantity(), completed.getPricePerUnit(), completed.getType(), completed.getUserId());
        pendingAfterReversal.setId(TRANSACTION_ID);
        pendingAfterReversal.setStatus(OrderStatus.PENDING); // Final status

        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.PENDING);

        // Mock: Find COMPLETED transaction, Stock reversal success, Save PENDING transaction
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(completed));
        when(productServiceClient.reverseProductStockUpdate(any(Transaction.class))).thenReturn(Mono.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingAfterReversal);

        // WHEN
        TransactionResponseDTO response = transactionService.updateTransactionStatus(
                TRANSACTION_ID, updateDTO, USER_ID
        );

        // THEN
        assertEquals(OrderStatus.PENDING, response.status());
        // Verify stock reversal was called
        verify(productServiceClient, times(1)).reverseProductStockUpdate(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void updateTransactionStatus_SimpleStatusChange_OnlySaves() {
        // GIVEN: Setup a PENDING transaction changing to SHIPPED (no stock impact)
        Transaction pending = pendingTransaction;
        Transaction shipped = new Transaction(pending.getProductId(), pending.getQuantity(), pending.getPricePerUnit(), pending.getType(), pending.getUserId());
        shipped.setId(TRANSACTION_ID);
        shipped.setStatus(OrderStatus.SHIPPED);
        
        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.SHIPPED);
        
        // Mock: Find PENDING transaction, Save SHIPPED transaction
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pending));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(shipped);

        // WHEN
        TransactionResponseDTO response = transactionService.updateTransactionStatus(
                TRANSACTION_ID, updateDTO, USER_ID
        );

        // THEN
        assertEquals(OrderStatus.SHIPPED, response.status());
        // Verify no stock-affecting calls were made
        verify(productServiceClient, never()).updateProductStock(any());
        verify(productServiceClient, never()).reverseProductStockUpdate(any());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}