package com.studioedge.domain.stat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateTotalStatColorRequest(
        @Schema(description = "과목 색상 (헥스 코드)", example = "#FF5733")
        @NotNull(message = "색상은 필수입니다.")
        String color
) {

}
