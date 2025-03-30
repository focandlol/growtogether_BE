package focandlol.domain.repository;


import focandlol.domain.entity.MemberEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // 이메일 중복 검사
    boolean existsByEmail(String email);

    // 닉네임 중복 검사
    boolean existsByNickName(String nickName);

    // 전화번호 중복 검사
    boolean existsByPhone(String phone);

    // 이메일로 회원 정보 조회 (로그인 시 사용)
    Optional<MemberEntity> findByEmail(String email);

    // 카카오 아이디로 서비스 회원 검증하기
    Optional<MemberEntity> findByKakaoId(String kakaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberEntity m WHERE m.memberId = :memberId")
    Optional<MemberEntity> findByIdWithLock(@Param("memberId") Long memberId);

    Optional<MemberEntity> findById(Long memberId);
    Optional<MemberEntity> findByPhone(String phone);
}