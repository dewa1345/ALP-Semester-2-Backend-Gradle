// Community.java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommunity;

    private String communityName;

    @ManyToOne
    @JoinColumn(name = "idCreator", referencedColumnName = "idCreator", nullable = true)
    @JsonBackReference
    private Creator creator;

    private String location;
    private String description;
    private int members;
    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;
}
