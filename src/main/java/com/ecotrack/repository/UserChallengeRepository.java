package com.ecotrack.repository;

import com.ecotrack.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, UUID> {
    List<UserChallenge> findByUserEmailAndStatus(String email , String status);
    boolean existsByUserEmailAndChallengeCodeAndStatus(String email , String code, String status);


    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.email = :email AND uc.status = 'ACTIVE'")
    List<UserChallenge> findActiveChallenges(String email);

    List<UserChallenge> findByUserEmailAndStatusIn(String email , List<String> statuses);

    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.email = :email AND uc.challenge.code = :code AND uc.status = 'PROPOSED'")
    Optional<UserChallenge> findProposedChallenge(@Param("email") String email ,@Param("code") String code);

}
