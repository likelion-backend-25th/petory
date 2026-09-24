package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.MemberProfileResponse;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final PostMapper postMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder, PostMapper postMapper) {
        this.memberMapper = memberMapper;
        this.postMapper = postMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member signup(SignUpRequest member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        LocalDateTime infoProvideAgreement = null;
        if (member.getIsAgreed()) {infoProvideAgreement = LocalDateTime.now();}
        memberMapper.createMember(member, infoProvideAgreement);
        return memberMapper.findByEmail(member.getEmail());
    }

    @Override
    public Member findMemberByEmail(String email) {
        return memberMapper.findByEmail(email);
    }

    @Override
    public Member findMemberById(Long id) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다");
        }
        return member;
    }

    @Override
    public MemberProfileResponse getMyProfile(Member member) {
        Long memberId = member.getId();
        long postCount = postMapper.countMyPosts(memberId);
        long followerCount = memberMapper.countFollowers(memberId);
        long followingCount = memberMapper.countFollowings(memberId);
        return MemberProfileResponse.from(member, postCount, followerCount, followingCount);
    }

    @Override
    public long countFollowers(Long memberId) {return memberMapper.countFollowers(memberId);}

    @Override
    public long countFollowings(Long memberId) {return memberMapper.countFollowings(memberId);}
}
