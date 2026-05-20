package com.stocksync.order_service.controller;

import com.stocksync.order_service.dto.TransactionRequestDTO;
import com.stocksync.order_service.dto.TransactionResponseDTO;
import com.stocksync.order_service.dto.TransactionStatusUpdateDTO;
import com.stocksync.order_service.model.TransactionType;
import com.stocksync.order_service.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// Import for HttpServletRequest
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin
class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private static final String USER_ID_HEADER = "X-Auth-User-Id";

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Authentication context missing. Header '" + USER_ID_HEADER + "' not found.");
        }
        return userId;
    }


    @PostMapping("/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO createSellOrder(
            @Validated
            @RequestBody TransactionRequestDTO requestDTO,
            HttpServletRequest request
    ) {

        String userId = getUserId(request);
        return transactionService.createTransaction(requestDTO, TransactionType.SELL, userId);
    }

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO createPurchaseOrder(
            @RequestBody TransactionRequestDTO requestDTO,
            HttpServletRequest request
    ) {

        String userId = getUserId(request);
        return transactionService.createTransaction(requestDTO, TransactionType.PURCHASE, userId);
    }

    @GetMapping
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionService.findAllTransactions();
    }

    @GetMapping("/{id}")
    public TransactionResponseDTO getTransactionById(@PathVariable BigInteger id) {
        return transactionService.findTransactionById(id);
    }


    @PatchMapping("/{id}/status")
    public TransactionResponseDTO updateTransactionStatus(
            @PathVariable BigInteger id,
            @RequestBody TransactionStatusUpdateDTO statusUpdateDTO,
            HttpServletRequest request
    ) {

        String updatingUserId = getUserId(request);
        log.info("Received status update for Transaction ID: {} to {} by User/Service: {}", id, statusUpdateDTO.newStatus(), updatingUserId);
        return transactionService.updateTransactionStatus(id, statusUpdateDTO, updatingUserId);
    }
}