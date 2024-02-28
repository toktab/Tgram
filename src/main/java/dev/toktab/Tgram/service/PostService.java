package dev.toktab.Tgram.service;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.repository.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final PostRepo postRepo;

    @Autowired
    public PostService(PostRepo postRepo) {
        this.postRepo = postRepo;
    }
    // CREATE
    public ResponseEntity<Object> create(Post post) {
        try {
            post.setCreatedOn(LocalDateTime.now());
            post.setUpdatedOn(LocalDateTime.now());
            postRepo.save(post);
            return ResponseEntity.status(HttpStatus.CREATED).body("New post created successfully");
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to create a new post");
        }
    }


    //READALL
    public ResponseEntity<Object> getAll() {
        List<Post> posts = postRepo.findAll();

        if (posts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: No posts found");
        } else {
            return ResponseEntity.ok(posts);
        }
    }

    //READ

    //DELETE
    public ResponseEntity<Object> delete(User user) {

        return null;
    }

    //UPDATE



}
