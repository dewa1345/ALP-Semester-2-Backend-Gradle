package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityController {

    private final CommunityRepository communityRepository;
    private final CommunityUserRepository communityUserRepository;
    private final CommunityMsgRepository communityMsgRepository;
    private final CreatorRepository creatorRepository;
    private final UserRepository userRepository;
    private final RatingUserRepository ratingUserRepository;

    @GetMapping
    public List<Map<String, Object>> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();

        return communities.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idCommunity", c.getIdCommunity());
            map.put("communityName", c.getCommunityName());
            map.put("description", c.getDescription());
            map.put("location", c.getLocation());
            map.put("members", c.getMembers());

            // Serve logo as preview URL if available
            if (c.getLogo() != null) {
                map.put("logoUrl", "/api/communities/photo/" + c.getIdCommunity());
            } else {
                map.put("logoUrl", null);
            }

            Creator creator = c.getCreator();
            Long creatorId = (creator != null && creator.getUser() != null) ? creator.getUser().getId_user() : null;
            map.put("creatorId", creatorId);

            return map;
        }).toList();
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserCommunities(@PathVariable Long userId) {
        List<CommunityUser> communityUsers = communityUserRepository.findByIdUser(userId);

        return communityUsers.stream().map(cu -> {
            Community community = cu.getCommunity();
            Map<String, Object> map = new HashMap<>();
            map.put("idCommunity", community.getIdCommunity());
            map.put("communityName", community.getCommunityName());
            map.put("description", community.getDescription());
            map.put("location", community.getLocation());
            map.put("members", community.getMembers());

            if (community.getLogo() != null) {
                map.put("logoUrl", "/api/communities/photo/" + community.getIdCommunity());
            } else {
                map.put("logoUrl", null);
            }

            Creator creator = community.getCreator();
            Long creatorId = (creator != null && creator.getUser() != null) ? creator.getUser().getId_user() : null;
            map.put("creatorId", creatorId);

            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Community> getCommunity(@PathVariable Long id) {
        return communityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCommunity(@RequestBody Map<String, Object> requestData) {
        String email = (String) requestData.get("email");
        String name = (String) requestData.get("communityName");
        String description = (String) requestData.get("description");
        String location = (String) requestData.get("location");

        try {
            if (communityRepository.findAll().stream().anyMatch(c -> c.getCommunityName().equalsIgnoreCase(name))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Community name already exists");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            Creator creator = new Creator();
            creator.setUser(user);
            creatorRepository.save(creator);

            Community community = new Community();
            community.setCommunityName(name);
            community.setDescription(description);
            community.setLocation(location);
            community.setMembers(1);
            community.setCreator(creator);
            Community savedCommunity = communityRepository.save(community);

            creator.setCommunity(savedCommunity);
            creatorRepository.save(creator);

            CommunityUserId id = new CommunityUserId();
            id.setUser(user.getId_user());
            id.setCommunity(savedCommunity.getIdCommunity());

            CommunityUser cu = new CommunityUser();
            cu.setId(id);
            cu.setUser(user);
            cu.setCommunity(savedCommunity);
            communityUserRepository.save(cu);

            System.out.println("Request payload: " + requestData);
            System.out.println("Extracted location: " + location);

            return ResponseEntity.ok(savedCommunity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Community creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/photo")
    public ResponseEntity<?> uploadCommunityLogo(
            @RequestParam("communityId") Long communityId,
            @RequestPart("photo") MultipartFile photoFile) {
        Optional<Community> opt = communityRepository.findById(communityId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Community not found");
        }

        try {
            Community community = opt.get();
            byte[] imageBytes = photoFile.getBytes();
            community.setLogo(imageBytes);
            communityRepository.save(community);
            return ResponseEntity.ok("Logo uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getCommunityLogo(@PathVariable Long id) {
        Optional<Community> opt = communityRepository.findById(id);
        if (opt.isEmpty() || opt.get().getLogo() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(opt.get().getLogo());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> joinCommunity(@PathVariable Long id, @RequestParam Long userId) {
        Community community = communityRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (community == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid user or community ID");
        }

        CommunityUserId cuId = new CommunityUserId();
        cuId.setUser(user.getId_user());
        cuId.setCommunity(community.getIdCommunity());

        CommunityUser cu = new CommunityUser();
        cu.setId(cuId);
        cu.setUser(user);
        cu.setCommunity(community);
        communityUserRepository.save(cu);

        community.setMembers(community.getMembers() + 1);
        communityRepository.save(community);

        return ResponseEntity.ok("Joined community");
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<String> leaveCommunity(@PathVariable Long id, @RequestParam Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Anda harus login untuk keluar dari komunitas.");
        }

        CommunityUserId key = new CommunityUserId();
        key.setUser(userId);
        key.setCommunity(id);

        if (communityUserRepository.existsById(key)) {
            communityUserRepository.deleteById(key);
            communityRepository.findById(id).ifPresent(c -> {
                c.setMembers(Math.max(0, c.getMembers() - 1));
                communityRepository.save(c);
            });
            return ResponseEntity.ok("Left community");
        } else {
            return ResponseEntity.status(404).body("User not in community");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCommunity(@PathVariable Long id) {
        Optional<Community> opt = communityRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Community not found");
        }
        Community community = opt.get();
        try {
            // Remove all related CommunityUser and CommunityMsg
            communityUserRepository.deleteAll(communityUserRepository.findByCommunity_IdCommunity(id));
            communityMsgRepository.deleteAll(communityMsgRepository.findByCommunity(community));
            // Break the association before deleting creator
            Creator creator = community.getCreator();
            if (creator != null) {
                community.setCreator(null);
                communityRepository.save(community);
                creatorRepository.deleteById(creator.getIdCreator());
            }
            communityRepository.deleteById(id);
            return ResponseEntity.ok("Community deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete community: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<String> sendMessage(@PathVariable Long id, @RequestParam Long userId, @RequestBody String message) {
        Community community = communityRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (community == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid user or community ID");
        }

        message = message.replaceAll("^\"|\"$", "");

        CommunityMsg msg = new CommunityMsg();
        msg.setCommunity(community);
        msg.setUser(user);
        msg.setCommunityMsg(message);
        msg.setTimestamp(LocalDateTime.now());

        communityMsgRepository.save(msg);
        return ResponseEntity.ok("Message sent");
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<CommunityMsg>> getMessages(@PathVariable Long id) {
        Community community = communityRepository.findById(id).orElse(null);
        if (community == null) return ResponseEntity.notFound().build();

        List<CommunityMsg> messages = communityMsgRepository.findByCommunity(community);
        messages.sort(Comparator.comparing(CommunityMsg::getTimestamp));

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getCommunityMembers(@PathVariable Long id) {
        List<CommunityUser> users = communityUserRepository.findByCommunity_IdCommunity(id);
        return users.stream().map(cu -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", cu.getUser().getName());
            map.put("email", cu.getUser().getEmail());
            map.put("hasPhoto", cu.getUser().getFotoKTP() != null);
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/all-with-status")
    public ResponseEntity<List<Map<String, Object>>> getCommunitiesWithStatus(@RequestParam Long userId) {
        List<Community> all = communityRepository.findAll();
        Set<Long> joinedIds = communityUserRepository.findByIdUser(userId).stream()
                .map(cu -> cu.getCommunity().getIdCommunity())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = all.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idCommunity", c.getIdCommunity());
            map.put("communityName", c.getCommunityName());
            map.put("description", c.getDescription());
            map.put("location", c.getLocation());
            map.put("members", c.getMembers());
            map.put("joined", joinedIds.contains(c.getIdCommunity()));
            map.put("logoUrl", c.getLogo() != null ? "/api/communities/photo/" + c.getIdCommunity() : null);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/star")
    public ResponseEntity<String> starCommunity(@PathVariable Long id, @RequestParam Long userId) {
        Community community = communityRepository.findById(id).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        RatingUserId key = new RatingUserId();
        key.setUserId(userId);
        key.setCommunityId(id);

        if (ratingUserRepository.existsById(key)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already starred");
        }

        RatingUser rating = new RatingUser();
        rating.setId(key);
        rating.setUser(user);
        rating.setCommunity(community);
        rating.setActive(true);
        ratingUserRepository.save(rating);

        // 👉 Increase rating count
        community.setRating(community.getRating() + 1);
        communityRepository.save(community);

        return ResponseEntity.ok("Community starred!");
    }

    // ❌ Unstar a community
    @DeleteMapping("/{id}/star")
    public ResponseEntity<String> unstarCommunity(@PathVariable Long id, @RequestParam Long userId) {
        RatingUserId key = new RatingUserId();
        key.setUserId(userId);
        key.setCommunityId(id);

        RatingUser rating = ratingUserRepository.findById(key).orElse(null);
        if (rating == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You haven't starred this community.");
        }

        ratingUserRepository.deleteById(key);

        Community community = communityRepository.findById(id).orElseThrow();
        // 👇 Ensure rating doesn't go below 0
        int newRating = Math.max(0, community.getRating() - 1);
        community.setRating(newRating);
        communityRepository.save(community);

        return ResponseEntity.ok("Star removed.");
    }

    
};