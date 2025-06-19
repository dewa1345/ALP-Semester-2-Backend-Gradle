package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.CommunityUser;
import com.example.demo.model.CommunityUserId;

public interface CommunityUserRepository extends JpaRepository<CommunityUser, CommunityUserId> {
    List<CommunityUser> findByIdUser(Long idUser);
    List<CommunityUser> findByCommunity_IdCommunity(Long idCommunity);

    // Query untuk mendapatkan idCommunity dengan jumlah follower terbanyak
    @Query("SELECT cu.community.idCommunity FROM CommunityUser cu GROUP BY cu.community.idCommunity ORDER BY COUNT(cu.user) DESC")
    Long findTopCommunityIdByFollowers();

    // Query untuk mendapatkan 5 idCommunity dengan jumlah follower terbanyak
    @Query("SELECT cu.community.idCommunity FROM CommunityUser cu GROUP BY cu.community.idCommunity ORDER BY COUNT(cu.user) DESC")
    List<Long> findTop5CommunityIdsByFollowers(org.springframework.data.domain.Pageable pageable);
}