package com.finance.dashboard.repository;

import com.finance.dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository gives you save(), findById(), findAll(), delete() for FREE
// You only write methods that aren't standard
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring reads this method name and generates the SQL automatically
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    //Because a user with that email might not exist. Optional forces you to handle the "not found" case explicitly, preventing NullPointerExceptions.
    // SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM users WHERE email = ?
    boolean existsByEmail(String email);
}