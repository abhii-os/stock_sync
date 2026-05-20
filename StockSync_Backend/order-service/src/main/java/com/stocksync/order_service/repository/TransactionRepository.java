package com.stocksync.order_service.repository;

import com.stocksync.order_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, BigInteger> {

}
