package net.likelion.bebc25.projectpatory.service;


import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.Profile;
import net.likelion.bebc25.projectpatory.dto.MemberProfileEditRequest;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;

public interface MemberService {
    Member signup(SignUpRequest member);

    Member findMemberByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Member findMemberById(Long id);

    Profile getMyProfile(Member member, Long loginMemberId);

    void editMyProfile(MemberProfileEditRequest request, Long loginMemberId, Long memberId);

    void deleteMyProfile(Long loginMemberId, Long memberId);
}
