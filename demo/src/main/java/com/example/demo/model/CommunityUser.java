package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CommunityUser {

    @EmbeddedId
    private CommunityUserId id;

    @ManyToOne
    @MapsId("user")
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private User user;

    @ManyToOne
    @MapsId("community")
    @JoinColumn(name = "id_community", referencedColumnName = "idCommunity")
    private Community community;
}
