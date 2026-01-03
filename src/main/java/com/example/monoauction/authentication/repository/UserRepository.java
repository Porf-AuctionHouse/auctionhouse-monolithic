package com.example.monoauction.authentication.repository;

import com.example.monoauction.authentication.model.entity.AppUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUsers, Long> {
    Boolean existsByEmail(String email);
    Optional<AppUsers> findByEmail(String email);
}
