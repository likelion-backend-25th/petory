package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {
    void saveRefreshToken(
            @Param("memberId") Long memberId,
            @Param("refreshToken") String refreshToken,
            @Param("expiration") LocalDateTime expiration
    );

    RefreshToken getRefreshToken(@Param("refreshToken") String refreshToken);

    void updateRefreshToken(
            @Param("id") Long id,
            @Param("memberId") Long memberId,
            @Param("refreshToken") String refreshToken,
            @Param("expiration") LocalDateTime expiration
    );
}
