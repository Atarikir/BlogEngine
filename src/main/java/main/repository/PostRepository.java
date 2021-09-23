package main.repository;

import main.model.Post;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {


    int countByIsActiveAndModerationStatusAndTimeBefore(byte isActive, ModerationStatus moderationStatus,
                                                        LocalDateTime time);

    List<Post> findPostsByIsActiveAndModerationStatusAndTimeBefore(byte isActive, ModerationStatus moderationStatus,
                                                                   LocalDateTime time, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.postComments pc " +
            "WHERE p.isActive = ?1 AND p.moderationStatus = ?2 AND p.time <= ?3 " +
            "GROUP BY p.id ORDER BY COUNT(pc.id) DESC")
    List<Post> getPostsByCommentsCount(byte isActive,
                                       ModerationStatus moderationStatus,
                                       LocalDateTime time,
                                       Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.postVotes pv " +
            "ON p.id = pv.post AND pv.value = 1 " +
            "WHERE p.isActive = ?1 AND p.moderationStatus = ?2 AND p.time <= ?3 " +
            "GROUP BY p.id ORDER BY COUNT(pv.id) DESC")
    List<Post> getPostsByLikesCount(byte isActive,
                                    ModerationStatus moderationStatus,
                                    LocalDateTime time,
                                    Pageable pageable);
}
