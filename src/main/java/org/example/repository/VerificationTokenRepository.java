package org.example.repository;

import org.example.entity.UserEntity;
import org.example.entity.VerificationToken;
import org.example.entity.VerificationToken.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    List<VerificationToken> findByUserAndTokenTypeAndUsedFalse(UserEntity user, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user AND v.tokenType = :tokenType")
    void deleteByUserAndTokenType(@Param("user") UserEntity user, @Param("tokenType") TokenType tokenType);
}
