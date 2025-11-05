package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import com.studioedge.focus_to_levelup_server.domain.system.entity.MemberAsset;
import com.studioedge.focus_to_levelup_server.global.common.enums.AssetType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public record CompleteSignUpRequest(
        @Schema(description = "나이", example = "24")
        @NotNull(message = "나이는 필수 항목입니다.")
        @Min(value = 8, message = "8세 이상만 가입할 수 있습니다.")
        @Max(value = 100, message = "100세 이하로 입력해주세요.")
        Integer age,

        @Schema(description = "성별(MALE, FEMALE)", example = "MALE")
        @NotNull(message = "성별은 필수 항목입니다.")
        Gender gender,

        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "메인 카테고리", example = "HIGH_SCHOOL")
        CategoryMainType categoryMain,

        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "서브 카테고리", example = "HIGH_2")
        CategorySubType categorySub,

        @Size(max = 50, message = "학교 이름은 50자를 초과할 수 없습니다.")
        @Schema(description = "학교 이름", example = "서울고등학교")
        String schoolName,

        @NotBlank(message = "닉네임은 필수 항목입니다.")
        @Size(min = 2, max = 16, message = "닉네임은 2자 이상, 16자 이하로 입력해야합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영어, 숫자만 사용 가능합니다. (특수문자, 공백 불가)")
        @Schema(description = "닉네임", example = "닉네임")
        String nickname

) {
    public static MemberInfo from(Member member, List<MemberAsset> initialAssets, CompleteSignUpRequest request) {
            MemberAsset imageAsset = initialAssets.stream()
                    .filter(ma -> ma.getAsset().getType() == AssetType.CHARACTER_PROFILE_IMAGE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("기본 프로필 이미지를 찾을 수 없습니다."));

            MemberAsset borderAsset = initialAssets.stream()
                    .filter(ma -> ma.getAsset().getType() == AssetType.CHARACTER_PROFILE_BORDER)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("기본 프로필 테두리를 찾을 수 없습니다."));

            return MemberInfo.builder()
                    .member(member)
                    .age(request.age())
                    .gender(request.gender())
                    .categoryMain(request.categoryMain())
                    .categorySub(request.categorySub())
                    .profileImage(imageAsset)
                    .profileBorder(borderAsset)
                    .belonging(request.schoolName() == null ? "없음" : request.schoolName())
                    .build();
    }
}
