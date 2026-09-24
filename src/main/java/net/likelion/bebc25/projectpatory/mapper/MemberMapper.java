package net.likelion.bebc25.projectpatory.mapper;


import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MemberMapper {
    // 회원가입
    void createMember(@Param("member") SignUpRequest member, @Param("infoProvideAgreement") LocalDateTime infoProvideAgreement);

    // 이메일 기반 회원 정보 조회
    Member findByEmail(@Param("email") String email);

    // 회원 ID로 정보 조회
    Member findById(@Param("id") Long id);

    // 회원 전체 조회
    List<Member> findAll();

    // 회원 계정 정지
    int updateStatusToBlocked(@Param("id") long id);

    // 회원 계정 삭제
    int deleteById(@Param("id") long id);
}