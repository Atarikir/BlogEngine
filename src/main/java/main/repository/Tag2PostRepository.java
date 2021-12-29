package main.repository;

import main.model.Tag2Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Integer> {

    int countByTagId(@NotNull int id);

    void deleteByPostId(@NotNull int postId);

    @Query("SELECT t2p.tagId FROM Tag2Post t2p WHERE t2p.tagId = ?1")
    Integer getTagId(@NotNull int id);
}
