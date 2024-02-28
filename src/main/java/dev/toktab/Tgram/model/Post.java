package dev.toktab.Tgram.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String picture;

    @Column(nullable = true)
    private String place;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @Column(name = "user_id") // Define the column name
    private Integer userId; // Change to Integer type for user_id

    // Constructor, getters, and setters
}
//        {
//        "title": "Example Post",
//        "picture": "example_picture.jpg",
//        "place": "Example Place",
//        "userId": 1
//        }
