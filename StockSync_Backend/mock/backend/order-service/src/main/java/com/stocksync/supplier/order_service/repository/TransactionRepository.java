package com.stocksync.supplier.order_service.repository;

import com.stocksync.supplier.order_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, BigInteger> {

}
