package net.likelion.bebc25.projectpatory.service;


import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.ProfileResponse;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;

public interface MemberService {
    Member signup(SignUpRequest member);

    Member findMemberByEmail(String email);

    Member findMemberById(Long id);

    ProfileResponse getMyProfile(Member member, Long loginMemberId);
}
