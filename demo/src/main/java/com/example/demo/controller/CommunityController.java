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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @GetMapping
    public List<Map<String, Object>> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();

        return communities.stream().map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("idCommunity", c.getIdCommunity());
            map.put("communityName", c.getCommunityName());
            map.put("description", c.getDescription());
            map.put("Location", c.getLocation());
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
        String Location = (String) requestData.get("Location");

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
            community.setLocation(Location);
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
            community.setLogo(imageBytes);  // store as byte[]
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

    @PostMapping("/{id}/messages")
    public ResponseEntity<String> sendMessage(@PathVariable Long id, @RequestParam Long userId, @RequestBody String message) {
        Community community = communityRepository.findById(id).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (community == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid user or community ID");
        }

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
        return ResponseEntity.ok(communityMsgRepository.findByCommunity(community));
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
}
