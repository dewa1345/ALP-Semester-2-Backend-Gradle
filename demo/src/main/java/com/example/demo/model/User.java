package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private boolean verified = false;

    public boolean isVerified() {
    return verified;
    }

    public void setVerified(boolean verified) {
    this.verified = verified;
    }

    @Column(name = "KTP_image", columnDefinition = "LONGBLOB")
    private byte[] fotoKTP;

    // Additional fields can be added as needed
}
