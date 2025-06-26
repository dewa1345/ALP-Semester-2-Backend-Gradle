package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RatingUser {

    @EmbeddedId
    private RatingUserId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private User user;

    @ManyToOne
    @MapsId("communityId")
    @JoinColumn(name = "id_community", referencedColumnName = "idCommunity")
    private Community community;

    private boolean active; // Optional - good for soft-deleting
}
