package net.likelion.bebc25.projectpatory.mapper;


import net.likelion.bebc25.projectpatory.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    // 이메일 기반 회원 정보 조회
    Member findByEmail(@Param("email") String email);
}