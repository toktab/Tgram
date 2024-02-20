package dev.toktab.Tgram.controller;

import dev.toktab.Tgram.model.Post;
import dev.toktab.Tgram.model.User;
import dev.toktab.Tgram.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    @PostMapping("/new")//create
    public ResponseEntity<Object> registerUser(@RequestBody Post post) {
        return postService.create(post);
    }


}
