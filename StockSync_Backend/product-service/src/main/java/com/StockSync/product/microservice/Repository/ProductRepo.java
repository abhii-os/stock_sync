package com.StockSync.product.microservice.Repository;

import com.StockSync.product.microservice.model.Product;
import org.hibernate.annotations.DialectOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

}
