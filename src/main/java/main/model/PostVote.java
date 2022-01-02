package main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_votes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    @Column(name = "time")
    @NotNull
    private LocalDateTime time;

    @Column(name = "value")
    @NotNull
    private byte value;
}