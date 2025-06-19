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
    private final PostUserRepository postUserRepo;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        if (post.getCommunity() != null) {
            Community community = communityRepo.findById(post.getCommunity().getIdCommunity()).orElse(null);
            if (community == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid community ID.");
            }
            post.setCommunity(community);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Community is required.");
        }

        try {
            Post saved = postRepo.save(post);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save post."+ e.getMessage());
        }
    }

    // ✅ Load chat messages
    @GetMapping("/{postId}/chat")
    public ResponseEntity<List<Map<String, Object>>> getChat(@PathVariable Long postId) {
        List<PostMessage> messages = msgRepo.findByPost_IdPost(postId);

        List<Map<String, Object>> response = new ArrayList<>();
        for (PostMessage msg : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("content", msg.getPostMsg());
            map.put("timestamp", msg.getTimestamp());

            User user = msg.getUser();
            Map<String, Object> sender = new HashMap<>();
            sender.put("name", user.getName());
            sender.put("email", user.getEmail());
            sender.put("avatarUrl", "/api/users/photo/" + user.getEmail());

            map.put("user", sender);
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    // ✅ Save chat message
    @PostMapping("/{postId}/chat")
    public ResponseEntity<?> addMessage(@PathVariable Long postId,
                                        @RequestParam String email,
                                        @RequestBody PostMessage message) {
        System.out.println("📨 Chat message incoming:");
        System.out.println("Post ID: " + postId);
        System.out.println("Email: " + email);
        System.out.println("Message body: " + message.getPostMsg());

        Post post = postRepo.findById(postId).orElse(null);
        User user = userRepo.findByEmail(email);

        if (post == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid post or user.");
        }

        if (message.getPostMsg() == null || message.getPostMsg().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message content is empty.");
        }

        message.setPost(post);
        message.setUser(user);
        message.setTimestamp(LocalDateTime.now());

        PostMessage saved = msgRepo.save(message);
        System.out.println("✅ Chat message saved with ID: " + saved.getId());

        return ResponseEntity.ok(saved);
    }

    // ✅ Communities created by user
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

    // ✅ Upload post photo
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

    // ✅ Get post photo
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

    // ✅ Join a campaign
    @PostMapping("/{postId}/follow")
    public ResponseEntity<String> followPost(@PathVariable Long postId, @RequestParam String email) {
        Post post = postRepo.findById(postId).orElse(null);
        User user = userRepo.findByEmail(email);

        if (post == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid post or user.");
        }

        PostUserId id = new PostUserId(post.getIdPost(), user.getId_user());
        if (postUserRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already joined.");
        }

        PostUser pu = new PostUser();
        pu.setId(id);
        pu.setPost(post);
        pu.setUser(user);
        postUserRepo.save(pu);

        post.setFollow(post.getFollow() + 1);
        postRepo.save(post);

        return ResponseEntity.ok("Successfully joined the campaign.");
    }

    // ✅ Unjoin a campaign
    @PostMapping("/{postId}/unfollow")
    public ResponseEntity<String> unfollowPost(@PathVariable Long postId, @RequestParam String email) {
        Post post = postRepo.findById(postId).orElse(null);
        User user = userRepo.findByEmail(email);

        if (post == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid post or user.");
        }

        PostUserId id = new PostUserId(post.getIdPost(), user.getId_user());
        if (!postUserRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not joined.");
        }

        postUserRepo.deleteById(id);
        post.setFollow(Math.max(0, post.getFollow() - 1));
        postRepo.save(post);

        return ResponseEntity.ok("Successfully unjoined the campaign.");
    }

    // ✅ Get posts a user has joined
    @GetMapping("/joined")
    public ResponseEntity<List<Post>> getJoinedPosts(@RequestParam String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        List<PostUser> joined = postUserRepo.findByUser(user);
        List<Post> posts = joined.stream().map(PostUser::getPost).toList();

        return ResponseEntity.ok(posts);
    }

    // ✅ Get event participants + creator
    @GetMapping("/{postId}/members")
    public ResponseEntity<List<Map<String, Object>>> getPostMembers(@PathVariable Long postId) {
        Optional<Post> optionalPost = postRepo.findById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = optionalPost.get();
        List<PostUser> postUsers = postUserRepo.findByPost_IdPost(postId);

        Set<Long> userIds = new HashSet<>();
        List<Map<String, Object>> members = new ArrayList<>();

        for (PostUser pu : postUsers) {
            User u = pu.getUser();
            userIds.add(u.getId_user());

            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId_user());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("avatarUrl", "/api/users/photo/" + u.getEmail());
            members.add(map);
        }

        // Include the creator if not already added
        List<Creator> creators = creatorRepository.findByCommunity(post.getCommunity());
        if (!creators.isEmpty()) {
            User creator = creators.get(0).getUser();
            if (!userIds.contains(creator.getId_user())) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", creator.getId_user());
                map.put("name", creator.getName());
                map.put("email", creator.getEmail());
                map.put("avatarUrl", "/api/users/photo/" + creator.getEmail());
                members.add(map);
            }
        }

        return ResponseEntity.ok(members);
    }

        @DeleteMapping("/{postId}")
        public ResponseEntity<?> deletePost(@PathVariable Long postId) {
            if (!postRepo.existsById(postId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found.");
            }

            postRepo.deleteById(postId);
            return ResponseEntity.ok("Post deleted.");
        }
}
