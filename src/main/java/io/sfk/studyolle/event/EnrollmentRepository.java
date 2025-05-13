package io.sfk.studyolle.event;

import io.sfk.studyolle.domain.Account;
import io.sfk.studyolle.domain.Enrollment;
import io.sfk.studyolle.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
