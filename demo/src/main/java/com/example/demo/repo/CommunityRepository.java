package com.example.demo.repo;

import com.example.demo.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findTop5ByOrderByMembersDesc(); // or .RatingDesc if you have a rating field
}
