package io.sfk.studyolle.study;

import io.sfk.studyolle.domain.Account;
import io.sfk.studyolle.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = repository.save(study);

        newStudy.addManager(account);
        return newStudy;
    }
}
