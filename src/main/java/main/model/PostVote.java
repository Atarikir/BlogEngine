package main.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "post_votes")
@Data
public class PostVote {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @ManyToOne()
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    @Column(name = "time")
    @NotNull
    private Date time;

    @Column(name = "value")
    @NotNull
    private byte value;
}