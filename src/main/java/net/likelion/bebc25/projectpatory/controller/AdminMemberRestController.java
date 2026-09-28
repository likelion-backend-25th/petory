package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.AdminMemberResponse;
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import net.likelion.bebc25.projectpatory.service.AdminMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Member API", description = "관리자 회원 관리 REST API")
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberRestController {

    private final AdminMemberService adminMemberService;

    // 관리자 회원 목록 조회
    @GetMapping
    @Operation(
            summary = "회원 목록 조회",
            description = "관리자가 전체 회원 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 목록 조회 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = AdminMemberResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<List<AdminMemberResponse>> getMembers() {

        List<AdminMemberResponse> response = adminMemberService.findAll()
                .stream()
                .map(AdminMemberResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    // 관리자 회원 계정 정지
    @PatchMapping("/{memberId}/block")
    @Operation(
            summary = "회원 계정 정지",
            description = "관리자가 특정 회원의 계정 상태를 BLOCKED로 변경한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "회원 계정 정지 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> blockMember(
            @Parameter(description = "정지할 회원 ID", example = "1")
            @PathVariable Long memberId
    ) {
        adminMemberService.blockMember(memberId);

        return ResponseEntity.noContent().build();
    }

}