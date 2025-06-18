package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Post;
import com.example.demo.model.PostMessage;

public interface PostMessageRepository extends JpaRepository<PostMessage, Long> {

    List<PostMessage> findByPost(Post post);
}
