package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

public record CreatePlannerListRequest(
        @Valid
        @Schema(description = "플래너 항목 리스트", example = """
                [
                    {
                        "subjectId": 2,
                        "startTime": "15:00:00",
                        "endTime": "16:00:00"
                    },
                    {
                         "subjectId": 5,
                         "startTime": "16:30:00",
                         "endTime": "17:00:00"
                    }
                ]
                """)
        List<CreatePlannerRequest> requestList
) {
}
