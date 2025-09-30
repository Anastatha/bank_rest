package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Page<Transfer> findByFromCard_User_IdOrToCard_User_Id(Long fromUserId, Long toUserId, Pageable pageable);

    Page<Transfer> findByFromCard_Id(Long cardId, Pageable pageable);
    Page<Transfer> findByToCard_Id(Long cardId, Pageable pageable);
}
