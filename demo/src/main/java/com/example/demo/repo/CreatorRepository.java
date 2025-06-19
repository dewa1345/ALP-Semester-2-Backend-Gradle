package com.example.demo.repo;

import com.example.demo.model.Community;
import com.example.demo.model.Creator;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
    List<Creator> findByUser(User user); // 🔧 Add this to support filtering by user
    List<Creator> findByCommunity(Community community); 
}
