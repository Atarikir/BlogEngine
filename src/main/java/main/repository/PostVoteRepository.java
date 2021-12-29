package main.repository;

import main.model.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {
    long countAllByValue(@NotNull byte value);
}
