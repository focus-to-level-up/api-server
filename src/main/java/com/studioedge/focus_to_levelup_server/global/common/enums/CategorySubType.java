package com.studioedge.focus_to_levelup_server.global.common.enums;

import lombok.Getter;

@Getter
public enum CategorySubType {
    // 초등 (ELEMENTARY)
    ELEMENTARY_1(CategoryMainType.ELEMENTARY_SCHOOL),
    ELEMENTARY_2(CategoryMainType.ELEMENTARY_SCHOOL),
    ELEMENTARY_3(CategoryMainType.ELEMENTARY_SCHOOL),
    ELEMENTARY_4(CategoryMainType.ELEMENTARY_SCHOOL),
    ELEMENTARY_5(CategoryMainType.ELEMENTARY_SCHOOL),
    ELEMENTARY_6(CategoryMainType.ELEMENTARY_SCHOOL),

    // 중등 (MIDDLE)
    MIDDLE_1(CategoryMainType.MIDDLE_SCHOOL),
    MIDDLE_2(CategoryMainType.MIDDLE_SCHOOL),
    MIDDLE_3(CategoryMainType.MIDDLE_SCHOOL),

    // 고등 (HIGH)
    HIGH_1(CategoryMainType.HIGH_SCHOOL),
    HIGH_2(CategoryMainType.HIGH_SCHOOL),
    HIGH_3(CategoryMainType.HIGH_SCHOOL),
    N_SU(CategoryMainType.HIGH_SCHOOL),

    // 성인 (ADULT)
    UNIVERSITY_STUDENT(CategoryMainType.ADULT),
    GRADUATE_STUDENT(CategoryMainType.ADULT),
    EXAM_TAKER(CategoryMainType.ADULT),
    PUBLIC_SERVANT(CategoryMainType.ADULT),
    JOB_SEEKER(CategoryMainType.ADULT),
    OFFICE_WORKER(CategoryMainType.ADULT);

    private final CategoryMainType mainType;

    CategorySubType(CategoryMainType mainType) {
        this.mainType = mainType;
    }
}
