package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Post;
import com.example.demo.model.User;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Optional: get all posts from a specific community
    List<Post> findByCommunityIdCommunity(Long idCommunity);

    // Optional: get posts followed by a specific user
    List<Post> findByFollowersContains(User user);
}
