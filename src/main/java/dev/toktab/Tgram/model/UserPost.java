package dev.toktab.Tgram.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_post_connect")
public class UserPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
