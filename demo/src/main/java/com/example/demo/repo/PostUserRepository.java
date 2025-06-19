package com.example.demo.repo;

import com.example.demo.model.PostUser;
import com.example.demo.model.PostUserId;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostUserRepository extends JpaRepository<PostUser, PostUserId> {

    boolean existsById(PostUserId id);

    List<PostUser> findByUser(User user);

    // ✅ This one works normally
    List<PostUser> findByPost_IdPost(Long idPost);
}
