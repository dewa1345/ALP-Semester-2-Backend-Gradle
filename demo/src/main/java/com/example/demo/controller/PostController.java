package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Post;
import com.example.demo.model.PostMessage;
import com.example.demo.model.User;
import com.example.demo.repo.PostMessageRepository;
import com.example.demo.repo.PostRepository;
import com.example.demo.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final PostMessageRepository postMsgRepo;

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postRepo.save(post);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        Post post = postRepo.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body("Post not found");
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followPost(@PathVariable Long id, @RequestParam String email) {
        Post post = postRepo.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        User user = userRepo.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        if (post.getFollowers() == null) {
            post.setFollowers(new ArrayList<>());
        }

        if (!post.getFollowers().contains(user)) {
            post.getFollowers().add(user);
            postRepo.save(post);
            return ResponseEntity.ok("User followed the post");
        } else {
            return ResponseEntity.ok("User already follows this post");
        }
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<?> addMessage(@PathVariable Long id, @RequestBody PostMessage message, @RequestParam String email) {
        Post post = postRepo.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        User user = userRepo.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        message.setPost(post);
        message.setUser(user);
        message.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(postMsgRepo.save(message));
    }

    @GetMapping("/{id}/chat")
    public List<PostMessage> getMessages(@PathVariable Long id) {
        Post post = postRepo.findById(id).orElse(null);
        return (post != null) ? postMsgRepo.findByPost(post) : List.of();
    }
}
