package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignUpRequest {
    @Schema(description = "회원의 email, 로그인 할 때 식별용으로 사용", example = "user1@example.com")
    private String email;

    @Schema(description = "회원의 비밀번호, 로그인 할 때 인증용으로 사용, BCrypt 방식의 암호화를 거침")
    private String password;

    @Schema(description = "반려동물 이름", example = "뭉치")
    private String nickname;

    @Schema(description = "반려동물 종류", example = "개")
    private String species;

    @Schema(description = "반려동물의 성별", example = "수")
    private String sex;

    @Schema(description = "반려동물의 생일", example = "2025-07-24")
    private LocalDate birthDate;

    @Schema(description = "자기소개", example = "안녕하세요")
    @Builder.Default
    private String intro = "안녕하세요";

    @Schema(description = "프로필 이미지가 저장된 S3 URL")
    @Builder.Default
    private String profileImage = "";

    @Schema(description = "회원의 거주지 정보, 개인정보 제공에 비동의 할 경우 타인에게 노출되지 않음, 분실동물 찾기 및 산책친구 찾기 등의 서비스 이용이 제한될 수 있음", example = "서울시 노원구 ...")
    @Builder.Default
    private String address = "";

    @Schema(description = "개인정보 제공 동의여부, 프론트에서 넘어온 체크박스의 값이 저장됨")
    private boolean isAgreed;
}
