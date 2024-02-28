package dev.toktab.Tgram.service;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.repository.PostRepo;
import dev.toktab.Tgram.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepo postRepo;
    private final UserService userService;

    @Autowired
    public PostService(PostRepo postRepo,UserService userService) {
        this.postRepo = postRepo;
        this.userService = userService;
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No posts found");
        } else {
            return ResponseEntity.ok(posts);
        }
    }

    //READ

    //DELETE
    public ResponseEntity<Object> delete(Optional<Post> postOptional) {
        // Check if the post exists
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Post not found");
        }
        Post post = postOptional.get();

        // Check if the current user is authorized to delete the post
        ResponseEntity<Object> authorizationResponse = canDeletePost(post);
        if (authorizationResponse != null) {
            return authorizationResponse;
        }
        try {
            postRepo.delete(post);//deleting
            return ResponseEntity.ok("Post deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Exception caught while deleting the post");
        }
    }

    public ResponseEntity<Object> canDeletePost(Post post) {
        ResponseEntity<Object> activeUserResponse = userService.getActiveUserDetails();
        User activeUser = userService.extractUserFromResponseEntity(activeUserResponse);
        // Check if the current user is the creator of the post
        if (post.getUserId() != activeUser.getId()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: You are not authorized to delete this post.");
        }
        return null;
    }

    public Optional<Post> getById(int postId) {
        return postRepo.findById(postId);
    }

    //UPDATE



}
