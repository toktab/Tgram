package dev.toktab.Tgram.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;//auto
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    private String password;
    private LocalDateTime createdOn;//auto
    private LocalDateTime updatedOn;//auto
    private boolean isEnabled;//auto
    private String picture;
    private String roles;

    @OneToMany(mappedBy = "user")
    private Set<Post> posts = new HashSet<>();
}
//post example
//    {
//            "username":"alcatras",
//            "email":"tabagaritoko834@gmail.com",
//            "password":"babusprocc1",
//            "picture":"https://miro.medium.com/v2/resize:fit:1400/1*2eBdh0vLZjUyCDF6x1EqvQ.png",
//            "roles":"ADMIN"
//    }
