package main.repository;

import main.model.Post;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    String GENERAL_FILTER_FOR_POSTS = "WHERE p.isActive = ?1 AND p.moderationStatus = ?2 AND p.time <= ?3";

    int countByIsActiveAndModerationStatusAndTimeBefore(byte isActive, ModerationStatus moderationStatus,
                                                        LocalDateTime time);

    Page<Post> findPostsByIsActiveAndModerationStatusAndTimeBefore(byte isActive, ModerationStatus moderationStatus,
                                                                   LocalDateTime time, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.postComments pc " + GENERAL_FILTER_FOR_POSTS +
            " GROUP BY p.id ORDER BY COUNT(pc.id) DESC")
    Page<Post> getPostsByCommentsCount(byte isActive, ModerationStatus moderationStatus, LocalDateTime time,
                                       Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.postVotes pv " +
            "ON p.id = pv.post AND pv.value = 1 " + GENERAL_FILTER_FOR_POSTS +
            " GROUP BY p.id ORDER BY COUNT(pv.id) DESC")
    Page<Post> getPostsByLikesCount(byte isActive, ModerationStatus moderationStatus, LocalDateTime time,
                                    Pageable pageable);

    @Query("SELECT p FROM Post p " + GENERAL_FILTER_FOR_POSTS + " AND (p.title LIKE %?4% OR p.text LIKE %?4%)")
    Page<Post> getPostByQuery(byte isActive, ModerationStatus moderationStatus, LocalDateTime time, String query,
                              Pageable pageable);

    @Query("SELECT DISTINCT YEAR(time) FROM Post p " + GENERAL_FILTER_FOR_POSTS)
    List<Integer> getYears(byte isActive, ModerationStatus moderationStatus, LocalDateTime time);

    @Query("SELECT DATE_FORMAT(time, '%Y-%m-%d') AS date, COUNT(time) FROM Post p " + GENERAL_FILTER_FOR_POSTS +
            " AND YEAR(time) = ?4 GROUP BY date ORDER BY date")
    List<Object[]> getPostCountInYearGroupByDate(byte isActive, ModerationStatus status, LocalDateTime time, int year);

    @Query("SELECT p FROM Post p " + GENERAL_FILTER_FOR_POSTS + " AND DATE_FORMAT(time, '%Y-%m-%d') = ?4")
    Page<Post> getPostByDate(byte isActive, ModerationStatus moderationStatus, LocalDateTime time, String date,
                             Pageable pageable);

    @Query("SELECT p FROM Post p JOIN Tag2Post t2p ON t2p.postId = p.id JOIN Tag t ON t2p.tagId = t.id " +
            GENERAL_FILTER_FOR_POSTS + " AND t.name = ?4")
    Page<Post> getPostByTag(byte isActive, ModerationStatus moderationStatus, LocalDateTime time, String tag,
                            Pageable pageable);

    Post findPostByIsActiveAndModerationStatusAndTimeBeforeAndId(byte isActive,
                                                                          ModerationStatus moderationStatus,
                                                                          LocalDateTime time, Integer id);
}