package main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @JoinColumn(name = "parent_id")
    private Integer parentId;

    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PostComment> postComments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @Column(name = "time")
    @NotNull
    private LocalDateTime time;

    @Column(name = "text", columnDefinition = "TEXT")
    @NotNull
    private String text;
}