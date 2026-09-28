package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.AdminMemberResponse;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminMemberServiceTest {

    @Autowired
    private AdminMemberService adminMemberService;

    @Autowired
    private MemberMapper memberMapper;


    @Test
    @DisplayName("관리자가 전체 회원 목록을 조회한다")
    void findAllTest() {

        // when
        List<Member> members =
                adminMemberService.findAll();

        // then
        assertThat(members).isNotEmpty();

        assertThat(members)
                .allSatisfy(member -> {
                    assertThat(member.getId()).isNotNull();
//                    assertThat(member.nickname()).isNotNull();
//                    assertThat(member.role()).isNotNull();
//                    assertThat(member.status()).isNotNull();
                });
    }


    @Test
    @DisplayName("관리자가 회원 계정을 정지한다")
    void blockMemberTest() {

        // given
        List<Member> members = memberMapper.findAll();
        assertThat(members).isNotEmpty();

        Long memberId = members.get(0).getId();

        // when
        adminMemberService.blockMember(memberId);

        // then
        Member member = memberMapper.findById(memberId);

        assertThat(member).isNotNull();
        assertThat(member.getStatus()).isEqualTo("BLOCKED");
    }


    @Test
    @DisplayName("관리자가 회원을 삭제한다")
    void deleteMemberTest() {

        // given
        List<Member> members = memberMapper.findAll();
        assertThat(members).isNotEmpty();

        Long memberId = members.get(0).getId();

        // when
        adminMemberService.deleteMember(memberId);

        // then
        Member member = memberMapper.findById(memberId);

        assertThat(member).isNull();
    }
}
