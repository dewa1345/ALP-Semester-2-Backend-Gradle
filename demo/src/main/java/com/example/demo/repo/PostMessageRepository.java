package com.example.demo.repo;

import com.example.demo.model.PostMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMessageRepository extends JpaRepository<PostMessage, Long> {
    List<PostMessage> findByPost_IdPost(Long postId);
}
