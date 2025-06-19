package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_msg")
public class PostMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_post", referencedColumnName = "idPost")
    @JsonIgnoreProperties({"messages", "followers", "postPic"})
    private Post post;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    @JsonIgnoreProperties({"posts", "messages", "password"})
    private User user;

    private String postMsg;

    private LocalDateTime timestamp;
}
