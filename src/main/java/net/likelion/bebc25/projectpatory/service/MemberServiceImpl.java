package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member signup(SignUpRequest member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        LocalDateTime infoProvideAgreement = null;
        if (member.isAgreed()) {infoProvideAgreement = LocalDateTime.now();}
        memberMapper.createMember(member, infoProvideAgreement);
        return memberMapper.findByEmail(member.getEmail());
    }

    @Override
    public Member findMemberByEmail(String email) {
        return memberMapper.findByEmail(email);
    }

    @Override
    public Member findMemberById(Long id) {
        return memberMapper.findById(id);
    }
}
