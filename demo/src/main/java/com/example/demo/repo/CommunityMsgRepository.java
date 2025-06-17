package com.example.demo.repo;

import com.example.demo.model.Community;
import com.example.demo.model.CommunityMsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityMsgRepository extends JpaRepository<CommunityMsg, Long> {
    List<CommunityMsg> findByCommunity(Community community); 
}