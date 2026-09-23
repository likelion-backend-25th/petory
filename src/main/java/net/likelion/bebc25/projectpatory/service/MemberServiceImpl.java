package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberMapper memberMapper) {this.memberMapper = memberMapper;}

    @Override
    public void signup(Member member) {
        memberMapper.createMember(member);
    }
}
