package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repo.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HomepageController {

    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final RatingUserRepository ratingUserRepository;
    private final CommunityUserRepository communityUserRepository;
    private final UserRepository userRepository;

    @GetMapping("/all-communities")
    public List<Map<String, Object>> getAllCommunities(@RequestParam(required = false) Long userId) {
        List<Community> allCommunities = communityRepository.findAll();

        Set<Long> joinedCommunityIds = (userId != null)
            ? communityUserRepository.findByIdUser(userId)
                .stream()
                .map(cu -> cu.getCommunity().getIdCommunity())
                .collect(Collectors.toSet())
            : Collections.emptySet();

        // Map each community to response and add starCount
        List<Map<String, Object>> mapped = allCommunities.stream()
            .map(c -> {
                Map<String, Object> map = mapCommunityToResponse(c, userId, joinedCommunityIds);
                // Count how many users starred this community
                int starCount = ratingUserRepository.countByIdCommunityIdAndActiveTrue(c.getIdCommunity());
                map.put("starCount", starCount);
                return map;
            })
            .collect(Collectors.toList());

        // Sort by starCount descending
        mapped.sort((a, b) -> Integer.compare((int) b.get("starCount"), (int) a.get("starCount")));

        return mapped;
    }

    private Map<String, Object> mapCommunityToResponse(Community c, Long userId, Set<Long> joinedIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("idCommunity", c.getIdCommunity());
        map.put("communityName", c.getCommunityName());
        map.put("location", c.getLocation());
        map.put("description", c.getDescription());
        map.put("members", c.getMembers());
        map.put("logoUrl", c.getLogo() != null ? "/api/communities/photo/" + c.getIdCommunity() : null);

        boolean isStarred = userId != null && ratingUserRepository
            .findByIdUserIdAndIdCommunityId(userId, c.getIdCommunity())
            .map(RatingUser::isActive)
            .orElse(false);

        boolean isJoined = joinedIds.contains(c.getIdCommunity());

        map.put("starred", isStarred);
        map.put("joined", isJoined);

        return map;
    }

    @GetMapping("/trending-posts")
    public List<Map<String, Object>> getTrendingPosts() {
        List<Post> trending = postRepository.findTop5ByOrderByFollowDesc();

        return trending.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idPost", p.getIdPost());
            map.put("title", p.getTitle());
            map.put("content", p.getContent());
            map.put("follow", p.getFollow());
            map.put("creatorEmail", p.getCreatorEmail());
            map.put("communityId", p.getCommunity().getIdCommunity());
            map.put("communityName", p.getCommunity().getCommunityName());
            map.put("location", p.getLocation());

            // ✅ Set postPic as base64 if exists, else default image path
            if (p.getPostPic() != null && p.getPostPic().length > 0) {
                String base64 = Base64.getEncoder().encodeToString(p.getPostPic());
                String dataUri = "data:image/jpeg;base64," + base64;
                map.put("postPic", dataUri);
            } else {
                map.put("postPic", "Gambar/post-default.avif");
            }

            return map;
        }).toList();
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinCommunity(@RequestParam Long userId, @RequestParam Long communityId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Community> communityOpt = communityRepository.findById(communityId);

        if (userOpt.isEmpty() || communityOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Community not found");
        }

        CommunityUserId id = new CommunityUserId();
        id.setUser(userId);
        id.setCommunity(communityId);

        if (communityUserRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already joined");
        }

        CommunityUser cu = new CommunityUser();
        cu.setId(id);
        cu.setUser(userOpt.get());
        cu.setCommunity(communityOpt.get());

        communityUserRepository.save(cu);

        return ResponseEntity.ok("Joined community successfully");
    }


        @DeleteMapping("/leave")
    public ResponseEntity<String> leaveCommunity(@RequestParam Long userId, @RequestParam Long communityId) {
        CommunityUserId id = new CommunityUserId();
        id.setUser(userId);
        id.setCommunity(communityId);

        if (!communityUserRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not a member of this community");
        }

        communityUserRepository.deleteById(id);
        return ResponseEntity.ok("Left community successfully");
    }

}
