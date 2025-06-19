// package com.example.demo.controller;

// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.demo.model.Community;
// import com.example.demo.repo.CommunityRepository;
// import com.example.demo.repo.CommunityUserRepository;

// @RestController
// @RequestMapping("/api/homepage")
// public class HomepageController {
//     @Autowired
//     private CommunityUserRepository communityUserRepository;
//     @Autowired
//     private CommunityRepository communityRepository;

//     @GetMapping("/top-community")
//     public Community getTopCommunity() {
//         Long topCommunityId = communityUserRepository.findTopCommunityIdByFollowers();
//         if (topCommunityId != null) {
//             return communityRepository.findById(topCommunityId).orElse(null);
//         }
//         return null;
//     }

//     @GetMapping("/top-communities")
//     public List<Community> getTopCommunities() {
//         List<Long> topCommunityIds = communityUserRepository.findTop5CommunityIdsByFollowers(PageRequest.of(0, 5));
//         List<Community> topCommunities = new ArrayList<>();
//         for (Long id : topCommunityIds) {
//             communityRepository.findById(id).ifPresent(topCommunities::add);
//         }
//         return topCommunities;
//     }
// }
