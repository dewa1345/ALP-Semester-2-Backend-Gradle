package com.example.demo.repo;

import com.example.demo.model.RatingUser;
import com.example.demo.model.RatingUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingUserRepository extends JpaRepository<RatingUser, RatingUserId> {

    // Find all ratings by a specific user
    List<RatingUser> findByIdUserId(Long userId);

    // Find all ratings for a specific community
    List<RatingUser> findByIdCommunityId(Long communityId);

    // Find a specific user's rating for a specific community
    Optional<RatingUser> findByIdUserIdAndIdCommunityId(Long userId, Long communityId);
    
}
