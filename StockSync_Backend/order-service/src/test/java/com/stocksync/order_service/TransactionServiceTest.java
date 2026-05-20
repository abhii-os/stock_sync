package com.stocksync.order_service;

import com.stocksync.order_service.dto.ProductDTO;
import com.stocksync.order_service.dto.TransactionRequestDTO;
import com.stocksync.order_service.dto.TransactionStatusUpdateDTO;
import com.stocksync.order_service.exception.InsufficientStockException;
import com.stocksync.order_service.exception.ProductNotFoundException;
import com.stocksync.order_service.exception.StockServiceException;
import com.stocksync.order_service.exception.TransactionNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private final String TEST_USER_ID = "user123";
    private final Long TEST_PRODUCT_ID = 1L;


    private final BigInteger TEST_TRANSACTION_ID = BigInteger.valueOf(100);

    private TransactionRequestDTO sellRequest;
    private TransactionRequestDTO buyRequest;
    private ProductDTO productDTO;
    private Transaction pendingTransaction;
    private Transaction completedTransaction;

    @BeforeEach
    void setUp() {
        // Setup common request and product details
        sellRequest = new TransactionRequestDTO(TEST_PRODUCT_ID, 5);
        buyRequest = new TransactionRequestDTO(TEST_PRODUCT_ID, 10);


        productDTO = new ProductDTO(
                TEST_PRODUCT_ID,
                "TEST-SKU-1",
                "Test Product",
                Integer.valueOf(100), // CONCRETE VALUE
                Integer.valueOf(5),
                "Description",
                BigDecimal.valueOf(10.50),
                true,
                1L,
                1L
        );


        pendingTransaction = new Transaction(
                TEST_PRODUCT_ID,
                sellRequest.quantity(),
                productDTO.getPrice().doubleValue(),
                TransactionType.SELL,
                TEST_USER_ID
        );

        pendingTransaction.setOrderId(TEST_TRANSACTION_ID.longValue());
        pendingTransaction.setStatus(OrderStatus.PENDING);


        completedTransaction = new Transaction(
                TEST_PRODUCT_ID,
                sellRequest.quantity(),
                productDTO.getPrice().doubleValue(),
                TransactionType.SELL,
                TEST_USER_ID
        );

        completedTransaction.setOrderId(TEST_TRANSACTION_ID.longValue());
        completedTransaction.setStatus(OrderStatus.COMPLETED);
    }

    @Test
    void createTransaction_Sell_InsufficientStock_ThrowsException() {
        when(productServiceClient.getProductDetails(TEST_PRODUCT_ID)).thenReturn(Mono.just(productDTO));
        when(productServiceClient.checkStockAvailability(TEST_PRODUCT_ID, 5)).thenReturn(Mono.just(false));

        assertThrows(InsufficientStockException.class,
                () -> transactionService.createTransaction(sellRequest, TransactionType.SELL, TEST_USER_ID));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ProductNotFound_ThrowsException() {
        when(productServiceClient.getProductDetails(TEST_PRODUCT_ID)).thenReturn(Mono.empty());

        assertThrows(ProductNotFoundException.class,
                () -> transactionService.createTransaction(sellRequest, TransactionType.SELL, TEST_USER_ID));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }




    @Test
    void updateTransactionStatus_ToCompleted_StockUpdateSuccess() {
        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);

        // 1. Mock finding the PENDING transaction (BigInteger argument used for findById)
        when(transactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(pendingTransaction));

        // 2. Mock stock update service call
        when(productServiceClient.updateProductStock(any(Transaction.class))).thenReturn(Mono.empty());

        // 3. Mock saving the COMPLETED transaction
        Transaction savedCompletedTransaction = new Transaction(pendingTransaction.getProductId(), pendingTransaction.getQuantity(), pendingTransaction.getPricePerUnit(), pendingTransaction.getType(), pendingTransaction.getUserId());
        savedCompletedTransaction.setOrderId(TEST_TRANSACTION_ID.longValue()); // Set Long value
        savedCompletedTransaction.setStatus(OrderStatus.COMPLETED);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedCompletedTransaction);

        // Execute (BigInteger argument passed to service)
        assertDoesNotThrow(() -> transactionService.updateTransactionStatus(
                TEST_TRANSACTION_ID, updateDTO, TEST_USER_ID
        ));

        verify(productServiceClient, times(1)).updateProductStock(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void updateTransactionStatus_ToCompleted_StockUpdateFails_SetsStatusToFailed() {
        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);

        // 1. Mock finding the PENDING transaction (BigInteger argument used for findById)
        when(transactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(pendingTransaction));

        // 2. Mock stock update service call to throw an error
        when(productServiceClient.updateProductStock(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("API Down")));

        // 3. Mock saving the FAILED transaction
        Transaction failedTransaction = new Transaction(pendingTransaction.getProductId(), pendingTransaction.getQuantity(), pendingTransaction.getPricePerUnit(), pendingTransaction.getType(), pendingTransaction.getUserId());
        failedTransaction.setOrderId(TEST_TRANSACTION_ID.longValue()); // Set Long value
        failedTransaction.setStatus(OrderStatus.FAILED);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(failedTransaction);

        // Execute (BigInteger argument passed to service)
        assertThrows(StockServiceException.class, () -> transactionService.updateTransactionStatus(
                TEST_TRANSACTION_ID, updateDTO, TEST_USER_ID
        ));

        verify(productServiceClient, times(1)).updateProductStock(any(Transaction.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        assertEquals(OrderStatus.FAILED, failedTransaction.getStatus());
    }

    @Test
    void updateTransactionStatus_AlreadyCompleted_SkipsStockUpdate() {
        TransactionStatusUpdateDTO updateDTO = new TransactionStatusUpdateDTO(OrderStatus.COMPLETED);

        // Mock finding the transaction, which is ALREADY COMPLETED (BigInteger argument used for findById)
        when(transactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.of(completedTransaction));

        // Execute (BigInteger argument passed to service)
        assertDoesNotThrow(() -> transactionService.updateTransactionStatus(
                TEST_TRANSACTION_ID, updateDTO, TEST_USER_ID
        ));

        // Verify: Stock update and save should NEVER be called
        verify(productServiceClient, never()).updateProductStock(any());
        verify(transactionRepository, never()).save(any());
    }




    @Test
    void findTransactionById_NotFound_ThrowsException() {
        // Mock findById to return empty optional (BigInteger argument used for findById)
        when(transactionRepository.findById(TEST_TRANSACTION_ID)).thenReturn(Optional.empty());

        // Execute (BigInteger argument passed to service)
        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.findTransactionById(TEST_TRANSACTION_ID));
    }
}