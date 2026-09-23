package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;

import java.util.List;

public interface AdminMemberService {
    // 회원 전체 조회
    List<Member> findAll();
    // 회원 계정 정지
    void blockMember(Long memberId);
    // 회원 계정 삭제
    void deleteMember(Long memberId);
}
