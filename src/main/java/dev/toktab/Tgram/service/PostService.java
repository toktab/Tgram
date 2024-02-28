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
        if (!canBeInteracted(post)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: You are not authorized to interact with this post.");
        }

        try {
            postRepo.delete(post); // Deleting
            return ResponseEntity.ok("Post deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Exception caught while deleting the post");
        }
    }

    public ResponseEntity<Object> update(Post oldPost, Post newPost) {
        // Ensure essential fields are not changed
        newPost.setId(oldPost.getId());
        newPost.setCreatedOn(oldPost.getCreatedOn());
        newPost.setUserId(oldPost.getUserId()); // Assuming userId cannot be changed

        // If title, picture, and place are null in the newPost, retain the old values
        if (newPost.getTitle() == null) {
            newPost.setTitle(oldPost.getTitle());
        }
        if (newPost.getPicture() == null) {
            newPost.setPicture(oldPost.getPicture());
        }
        if (newPost.getPlace() == null) {
            newPost.setPlace(oldPost.getPlace());
        }

        // Set updatedOn to current timestamp
        newPost.setUpdatedOn(LocalDateTime.now());

        // Check if any modification was made
        if (oldPost.equals(newPost)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error: Nothing modified");
        }

        // Check if the current user is authorized to update the post
        if (!canBeInteracted(oldPost)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: You are not authorized to update this post.");
        }

        // Save the updated post
        try {
            postRepo.save(newPost);
            return ResponseEntity.ok("Successfully updated post with ID: " + newPost.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to update the post");
        }
    }



    private boolean canBeInteracted(Post post) {
        ResponseEntity<Object> activeUserResponse = userService.getActiveUserDetails();
        User activeUser = userService.extractUserFromResponseEntity(activeUserResponse);
        return activeUser != null && post.getUserId() == activeUser.getId();
    }

    public Optional<Post> getById(int postId) {
        return postRepo.findById(postId);
    }
}
