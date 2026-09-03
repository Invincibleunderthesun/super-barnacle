package com.harsh.firstApp.repository;

import com.harsh.firstApp.model.Address;
import com.harsh.firstApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
