package org.example.bankingapi.repository;

import org.example.bankingapi.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // UC-06: Query both from and to accounts using OR - returns paginated results sorted by latest
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    Optional<Transaction> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);
}
