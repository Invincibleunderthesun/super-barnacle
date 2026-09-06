package com.harsh.uday.repository;

import com.harsh.uday.model.Seller;
import com.harsh.uday.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(User user);

    Optional<Seller> findByStoreName(String storeName);

    List<Seller> findByVerifiedTrue();

    List<Seller> findByActiveTrue();

    Page<Seller> findByVerifiedTrueAndActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(s) FROM Seller s WHERE s.verified = true AND s.active = true")
    long countActiveSellers();

    boolean existsByUser(User user);
}
