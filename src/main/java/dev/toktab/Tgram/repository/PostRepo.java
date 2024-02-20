package dev.toktab.Tgram.repository;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post, Integer> {
    @Query(value = "SELECT * FROM posts WHERE user_id = ?1", nativeQuery = true)
    Optional<Post> findByUserId(int user_id);

    @Query(value = "SELECT * FROM posts WHERE title = ?1", nativeQuery = true)
    Optional<Post> findByTitle(String title);

    @Query(value = "SELECT * FROM posts WHERE id = ?1", nativeQuery = true)
    Optional<Post> findById(int id);
}
