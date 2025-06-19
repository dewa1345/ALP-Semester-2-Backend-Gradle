package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPost;

    private String title;

    @Lob
    @Column(name = "post_pic", columnDefinition = "LONGBLOB")
    private byte[] postPic;

    private String content;

    private int follow;

    @ManyToOne
    @JoinColumn(name = "id_community", referencedColumnName = "idCommunity")
    @JsonIgnoreProperties({"location", "description", "members"})
    private Community community;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostMessage> messages;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private String location;
}
