package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PostUser {

    @EmbeddedId
    private PostUserId id;

    @ManyToOne
    @MapsId("user") // <-- must match PostUserId field
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private User user;

    @ManyToOne
    @MapsId("post") // <-- must match PostUserId field
    @JoinColumn(name = "id_post", referencedColumnName = "idPost")
    private Post post;
}

