package com.example.demo.repo;

import com.example.demo.model.CommunityUser;
import com.example.demo.model.CommunityUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityUserRepository extends JpaRepository<CommunityUser, CommunityUserId> {
    List<CommunityUser> findByIdUser(Long idUser);
    List<CommunityUser> findByCommunity_IdCommunity(Long idCommunity);
}