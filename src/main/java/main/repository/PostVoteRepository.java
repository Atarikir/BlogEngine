package main.repository;

import main.model.Post;
import main.model.PostVote;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {
    long countAllByValue(@NotNull byte value);

    PostVote findByUserAndPost(User user, Post post);

    void deleteById(int id);
}
