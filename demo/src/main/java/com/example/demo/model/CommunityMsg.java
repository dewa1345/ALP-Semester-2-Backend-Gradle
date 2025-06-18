// CommunityMsg.java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class CommunityMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idCommunity", referencedColumnName = "idCommunity")
    @JsonIgnoreProperties({"members", "messages"})
    private Community community;

    @ManyToOne
    @JoinColumn(name = "idUser", referencedColumnName = "id_user")
    @JsonIgnoreProperties({"communities", "password", "messages"})
    private User user;

    private String communityMsg;
    private LocalDateTime timestamp;
}
