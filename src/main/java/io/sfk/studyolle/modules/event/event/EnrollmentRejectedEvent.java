package io.sfk.studyolle.modules.event.event;

import io.sfk.studyolle.modules.event.Enrollment;

public class EnrollmentRejectedEvent extends EnrollmentEvent{

    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 거절했습니다.");
    }
}
