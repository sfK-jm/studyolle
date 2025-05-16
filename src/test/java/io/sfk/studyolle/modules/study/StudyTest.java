package io.sfk.studyolle.modules.study;

import io.sfk.studyolle.modules.account.UserAccount;
import io.sfk.studyolle.modules.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {

    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        account = new Account();
        account.setNickname("test_user");
        account.setPassword("12345678");
        userAccount = new UserAccount(account);
    }

    @Test
    @DisplayName("스터디를 공개했고 인원 모집 중이고, 이미 멤버나 스터디 관리자가 아니라면 스터디 가입 가능")
    void isJoinAble() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinAble(userAccount));
    }

    @Test
    @DisplayName("스터디를 공개했고 인원 모집 중이라도, 스터디 관리자는 스터디 가입이 불필요하다")
    void isJoinAble_false_for_manager() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinAble(userAccount));
    }

    @Test
    @DisplayName("스터디 관리자인지 확인")
    void isManager() {
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }

    @Test
    @DisplayName("스터디 멤버인지 확인")
    void isMember() {
        study.addMember(account);
        assertTrue(study.isMember(userAccount));
    }
}