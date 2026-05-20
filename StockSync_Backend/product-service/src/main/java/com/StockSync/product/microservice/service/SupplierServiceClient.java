package com.StockSync.product.microservice.service;

import com.StockSync.product.microservice.DTO.SupplierDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class SupplierServiceClient {

    private final WebClient supplierWebClient;

    public boolean validateSupplierExists(Long supplierId) {
        if (supplierId == null) {
            return false;
        }

        try {
            return supplierWebClient
                    .get()
                    .uri("/{id}", supplierId)
                    .retrieve()
                    .bodyToMono(SupplierDTO.class)
                    .blockOptional()
                    .isPresent();
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            throw new RuntimeException("Error validating supplier: " + e.getMessage());
        }
    }

    public SupplierDTO getSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }

        try {
            return supplierWebClient
                    .get()
                    .uri("/{id}", supplierId)
                    .retrieve()
                    .bodyToMono(SupplierDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        }
    }
}
