package com.example.monoauction.user.repository;

import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByIsActiveTrue();
}
