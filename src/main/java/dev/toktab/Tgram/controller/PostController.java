package dev.toktab.Tgram.controller;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.service.PostService;
import dev.toktab.Tgram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
