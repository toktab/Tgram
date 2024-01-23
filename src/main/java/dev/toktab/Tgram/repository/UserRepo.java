package dev.toktab.Tgram.repository;

import dev.toktab.Tgram.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    @Query(value = "SELECT * FROM users WHERE username = ?1", nativeQuery = true)
    Optional<User> findByUsername(String username);
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmail(String email);
    @Query(value = "SELECT * FROM users WHERE id = ?1", nativeQuery = true)
    Optional<User> findById(int id);


    @Query(value = "SELECT username FROM users", nativeQuery = true)
    List<String> findAllUsername();
    @Query(value = "SELECT email FROM users", nativeQuery = true)
    List<String> findAllEmail();
}
