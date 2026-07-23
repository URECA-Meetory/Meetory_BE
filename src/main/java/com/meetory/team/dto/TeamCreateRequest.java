package com.meetory.team.dto;

import com.meetory.team.entity.TeamCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamCreateRequest(

    @NotBlank(message = "모임 이름은 필수입니다")
    String title,

    @NotNull(message = "카테고리는 필수입니다")
    TeamCategory category,

    @NotBlank(message = "모임 소개는 필수입니다")
    String description,

    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다")
    @Max(value = 100, message = "최대 인원은 100명 이하여야 합니다")
    int maxMembers

) {
}
