package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberServiceImpl implements AdminMemberService{

    private final MemberMapper memberMapper;

    @Override
    public List<Member> findAll() {
        return memberMapper.findAll();
    }

    @Override
    public void blockMember(Long memberId) {

        int result = memberMapper.updateStatusToBlocked(memberId);

        if (result == 0){
            throw new IllegalArgumentException("존재하지 않는 회원입니다");
        }
    }

    @Override
    public void deleteMember(Long memberId) {

        int result = memberMapper.deleteById(memberId);

        if (result == 0) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
    }

    }
}
