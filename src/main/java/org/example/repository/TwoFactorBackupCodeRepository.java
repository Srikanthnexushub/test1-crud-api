package org.example.repository;

import org.example.entity.TwoFactorBackupCode;
import org.example.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TwoFactorBackupCodeRepository extends JpaRepository<TwoFactorBackupCode, Long> {

    List<TwoFactorBackupCode> findByUserAndUsedFalse(UserEntity user);

    @Query("SELECT COUNT(b) FROM TwoFactorBackupCode b WHERE b.user = :user AND b.used = false")
    int countUnusedByUser(@Param("user") UserEntity user);

    @Modifying
    @Query("DELETE FROM TwoFactorBackupCode b WHERE b.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
