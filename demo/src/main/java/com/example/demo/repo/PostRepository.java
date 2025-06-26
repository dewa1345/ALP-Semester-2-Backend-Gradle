package com.example.demo.repo;

import com.example.demo.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCommunity_IdCommunity(Long idCommunity);

    List<Post> findTop5ByOrderByFollowDesc();
}

