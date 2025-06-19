package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostRepository postRepo;
    private final PostMessageRepository msgRepo;
    private final UserRepository userRepo;
    private final CommunityRepository communityRepo;
    private final CreatorRepository creatorRepository;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        System.out.println("📥 Received new post:");
        System.out.println("Title: " + post.getTitle());
        System.out.println("Community ID: " + (post.getCommunity() != null ? post.getCommunity().getIdCommunity() : "null"));

        if (post.getCommunity() != null) {
            Community community = communityRepo.findById(post.getCommunity().getIdCommunity()).orElse(null);
            if (community == null) {
                System.out.println("❌ Community not found, post not saved.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid community ID.");
            }
            post.setCommunity(community);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Community is required.");
        }

        try {
            Post saved = postRepo.save(post);
            System.out.println("✅ Post saved with ID: " + saved.getIdPost());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save post.");
        }
    }

    

    @GetMapping("/{postId}/chat")
    public ResponseEntity<List<PostMessage>> getChat(@PathVariable Long postId) {
        return ResponseEntity.ok(msgRepo.findByPost_IdPost(postId));
    }

    @PostMapping("/{postId}/chat")
    public ResponseEntity<PostMessage> addMessage(@PathVariable Long postId,
                                                  @RequestParam String email,
                                                  @RequestBody PostMessage message) {
        Post post = postRepo.findById(postId).orElse(null);
        User user = userRepo.findByEmail(email);

        if (post == null || user == null) {
            return ResponseEntity.badRequest().build();
        }

        message.setPost(post);
        message.setUser(user);
        message.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(msgRepo.save(message));
    }

    @GetMapping("/communities/created")
    public ResponseEntity<List<Map<String, Object>>> getCommunitiesCreatedByUser(@RequestParam String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        List<Creator> creators = creatorRepository.findByUser(user);
        List<Map<String, Object>> createdCommunities = creators.stream()
                .filter(c -> c.getCommunity() != null)
                .map(c -> {
                    Community community = c.getCommunity();
                    Map<String, Object> map = new HashMap<>();
                    map.put("idCommunity", community.getIdCommunity());
                    map.put("communityName", community.getCommunityName());
                    return map;
                }).toList();

        return ResponseEntity.ok(createdCommunities);
    }

    @PutMapping("/photo")
    public ResponseEntity<?> uploadPostPhoto(@RequestParam("postId") Long postId,
                                             @RequestPart("photo") MultipartFile photoFile) {
        Optional<Post> opt = postRepo.findById(postId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        try {
            Post post = opt.get();
            byte[] imageBytes = photoFile.getBytes();
            post.setPostPic(imageBytes);
            postRepo.save(post);
            return ResponseEntity.ok("Post image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @GetMapping("/photo/{postId}")
    public ResponseEntity<byte[]> getPostPhoto(@PathVariable Long postId) {
        Optional<Post> opt = postRepo.findById(postId);
        if (opt.isEmpty() || opt.get().getPostPic() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(opt.get().getPostPic());
    }
}
