package main.repository;

import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    String GENERAL_FILTER_FOR_POSTS = "WHERE p.isActive = ?1 AND p.moderationStatus = ?2 AND p.time <= ?3";

    int countByIsActiveAndModerationStatusAndTimeBefore(byte isActive, ModerationStatus moderationStatus,
                                                        LocalDateTime time);

    int countByIsActiveAndModerationStatus(byte isActive, ModerationStatus moderationStatus);

    long count();

    long countByUser(User user);

    @Query("SELECT SUM(p.viewCount) FROM Post p WHERE p.user = ?1")
    Optional<Long> getMyViewsCount(User user);

    @Query("SELECT SUM(p.viewCount) FROM Post p")
    Optional<Long> getAllViewsCount();

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

    Page<Post> findPostsByIsActiveAndUser(byte isActive, User user, Pageable pageable);

    Page<Post> findPostsByIsActiveAndModerationStatusAndUser(byte isActive, ModerationStatus moderationStatus, User user,
                                                             Pageable pageable);

    Page<Post> findPostsByIsActiveAndModerationStatus(byte isActive, ModerationStatus moderationStatus, Pageable pageable);

    Page<Post> findPostsByIsActiveAndModerationStatusAndModeratorId(byte isActive, ModerationStatus moderationStatus,
                                                                    int userId, Pageable pageable);

    Post findById(int id);

    @Query(value = "SELECT p.time FROM posts p ORDER BY p.time ASC LIMIT 1", nativeQuery = true)
    LocalDateTime getFirstPublicationTime();

    @Query(value = "SELECT p.time FROM posts p WHERE p.is_active = '1' AND p.moderation_status = 'ACCEPTED' AND " +
            "p.time <= NOW() AND p.user_id = :user_id ORDER BY p.`time` ASC LIMIT 1", nativeQuery = true)
    LocalDateTime getMyFirstPublicationTime(@Param("user_id") User user);

    List<Post> findPostsByIsActiveAndModerationStatusAndUser(byte isActive, ModerationStatus moderationStatus, User user);
}
