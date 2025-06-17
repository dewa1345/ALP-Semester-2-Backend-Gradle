package com.example.demo.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class CommunityUserId implements Serializable {
    private Long user;       // refers to User.id_user
    private Long community;  // refers to Community.idCommunity
}
