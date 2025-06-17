// Creator.java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Creator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCreator;

    @ManyToOne
    @JoinColumn(name = "idUser", referencedColumnName = "id_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "idCommunity", referencedColumnName = "idCommunity")
    @JsonManagedReference
    private Community community;
}
