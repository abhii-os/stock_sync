package com.StockSync.product.microservice.service.impl;

import com.StockSync.product.microservice.DTO.StockUpdatePayload;
import com.StockSync.product.microservice.service.OrderServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderServiceClientImpl implements OrderServiceClient {

    private final RestClient restClient;
    private final String orderServiceBaseUrl;

    public OrderServiceClientImpl(RestClient restClient,
                                @Value("${order.service.url}") String orderServiceBaseUrl) {
        this.restClient = restClient;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    @Override
    public StockUpdatePayload getOrderDetails(Long productId) {
        return restClient.get()
                .uri(orderServiceBaseUrl + "/api/v1/transactions/product/" + productId)
                .retrieve()
                .body(StockUpdatePayload.class);
    }
}
