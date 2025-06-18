package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        map.put("address", c.getAddress());
        map.put("logo", c.getLogo());
        map.put("members", c.getMembers());

        // Get creator.user.id_user
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
        String logo = (String) requestData.get("logo");
        String address = (String) requestData.get("address");

        if (communityRepository.findAll().stream().anyMatch(c -> c.getCommunityName().equalsIgnoreCase(name))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Community name already exists");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Creator creator = new Creator();
        creator.setUser(user);
        creatorRepository.save(creator);

        Community community = new Community();
        community.setCommunityName(name);
        community.setDescription(description);
        community.setLogo(logo);
        community.setAddress(address);
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

    @GetMapping("/user/{userId}")
    public List<CommunityUser> getUserCommunities(@PathVariable Long userId) {
        return communityUserRepository.findByIdUser(userId);
    }
}
