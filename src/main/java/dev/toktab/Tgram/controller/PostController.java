package dev.toktab.Tgram.controller;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.service.PostService;
import dev.toktab.Tgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping("/new")//create
    public ResponseEntity<Object> newPost(@RequestBody Post post) {
        ResponseEntity<Object> userObject = userService.getActiveUserDetails();
        if (!userService.isEnabled(userObject)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User disabled. Please log out.\n'/logout");
        }
        User user = userService.extractUserFromResponseEntity(userObject);
        post.setUserId(user.getId());
        return postService.create(post);
    }
    @GetMapping("/feed/all")//all posts
    public ResponseEntity<Object> getAllPosts() {
        return postService.getAll();
    }

    @GetMapping("/feed/{postId}") // read post by ID
    public ResponseEntity<Object> getPostById(@PathVariable("postId") int postId) {
        Optional<Post> post = postService.getById(postId);
        if (post.isPresent()) {
            return ResponseEntity.ok(post.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Post not found with ID: " + postId);
        }
    }

    @DeleteMapping("/feed/{postId}/delete") // delete
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> deletePost(@RequestParam(name = "confirm", defaultValue = "false") boolean confirm, @PathVariable("postId") int postId) {
        if (!confirm) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, "/profile");
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }
        ResponseEntity<Object> activeUserResponse = userService.getActiveUserDetails();
        User activeUser = userService.extractUserFromResponseEntity(activeUserResponse);
        // Check if the user is enabled
        if (!userService.isEnabled(activeUserResponse)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User disabled. Please log out.\n'/logout");
        }
        // Get the post by postId
        Optional<Post> post = postService.getById(postId);
        if (post.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Post not found with ID: " + postId);
        }
        return postService.delete(post);
    }
    @PutMapping("/feed/{postId}/edit") // Update
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Object> updatePost(@RequestBody Post newPost, @PathVariable("postId") int postId) {
        // Get the current user details
        ResponseEntity<Object> activeUserResponse = userService.getActiveUserDetails();
        User activeUser = userService.extractUserFromResponseEntity(activeUserResponse);

        // Check if the user is enabled
        if (!userService.isEnabled(activeUserResponse)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User disabled. Please log out.\n'/logout");
        }

        // Get the post by postId
        Optional<Post> postOptional = postService.getById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Post not found with ID: " + postId);
        }
        Post oldPost = postOptional.get();

        // Update the post
        return postService.update(oldPost, newPost);
    }

}
