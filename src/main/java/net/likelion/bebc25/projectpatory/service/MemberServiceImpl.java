package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.exception.DuplicateResourceException;
import net.likelion.bebc25.projectpatory.domain.MemberProfile;
import net.likelion.bebc25.projectpatory.domain.MyProfile;
import net.likelion.bebc25.projectpatory.domain.Profile;
import net.likelion.bebc25.projectpatory.dto.MemberProfileEditRequest;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.FollowMapper;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final PostMapper postMapper;
    private final FollowMapper followMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder,
                             PostMapper postMapper, FollowMapper followMapper) {
        this.memberMapper = memberMapper;
        this.postMapper = postMapper;
        this.followMapper = followMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member signup(SignUpRequest member) {
        String email = member.getEmail() == null ? "" : member.getEmail().trim();
        String nickname = member.getNickname() == null ? "" : member.getNickname().trim();
        member.setEmail(email);
        member.setNickname(nickname);
        if (memberMapper.existsByEmail(email)) {
            throw new DuplicateResourceException("이미 가입된 이메일입니다.");
        }
        if (memberMapper.existsByNickname(nickname)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        LocalDateTime infoProvideAgreement = null;
        if (Boolean.TRUE.equals(member.getIsAgreed())) {
            infoProvideAgreement = LocalDateTime.now();
        }
        try {
            memberMapper.createMember(member, infoProvideAgreement);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("이미 가입된 이메일입니다.");
        }
        return memberMapper.findByEmail(email);
    }

    @Override
    public Member findMemberByEmail(String email) {
        return memberMapper.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberMapper.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberMapper.existsByNickname(nickname);
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
    public Profile getMyProfile(Member member, Long loginMemberId) {
        Long memberId = member.getId();
        long postCount = postMapper.countMyPosts(memberId);
        long followerCount = memberMapper.countFollowers(memberId);
        long followingCount = memberMapper.countFollowings(memberId);

        if (memberId.equals(loginMemberId)) {
            return MyProfile.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .species(member.getSpecies())
                    .sex(member.getSex())
                    .birthDate(member.getBirthDate())
                    .intro(member.getIntro())
                    .profileImage(member.getProfileImage())
                    .address(member.getAddress())
                    .status(member.getStatus())
                    .role(member.getRole())
                    .createdAt(member.getCreatedAt())
                    .infoProvideAgreement(member.getInfoProvideAgreement())
                    .postsCount(postCount)
                    .followers(followerCount)
                    .followings(followingCount)
                    .build();
        }
        boolean isFollowing = loginMemberId != null && followMapper.existsFollow(loginMemberId, memberId);
        return MemberProfile.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .intro(member.getIntro())
                .profileImage(member.getProfileImage())
                .status(member.getStatus())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .postsCount(postCount)
                .followers(followerCount)
                .followings(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    @Override
    public void editMyProfile(MemberProfileEditRequest request, Long loginMemberId, Long memberId) {
        if (!loginMemberId.equals(memberId)) {
            throw new IllegalStateException("비정상적인 접근입니다.");
        }
        if (memberMapper.findById(memberId) == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        memberMapper.editMyProfile(request, memberId);
    }

    @Override
    public void deleteMyProfile(Long loginMemberId, Long memberId) {
        if (!loginMemberId.equals(memberId)) {
            throw new IllegalStateException("비정상적인 접근입니다.");
        }
        if (memberMapper.findById(memberId) == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        memberMapper.deleteById(memberId);
    }
}
