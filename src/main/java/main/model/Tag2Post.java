package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tag2post")
@Data
@NoArgsConstructor
public class Tag2Post {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private int id;

    @Column(name = "post_id")
    @NotNull
    private int postId;

    @Column(name = "tag_id")
    @NotNull
    private int tagId;
}