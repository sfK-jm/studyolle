package io.sfk.studyolle.modules.event;

import io.sfk.studyolle.infra.AbstractContainerBaseTest;
import io.sfk.studyolle.infra.MockMvcTest;
import io.sfk.studyolle.modules.account.AccountFactory;
import io.sfk.studyolle.modules.account.AccountRepository;
import io.sfk.studyolle.modules.account.WithAccount;
import io.sfk.studyolle.modules.account.Account;
import io.sfk.studyolle.modules.study.Study;
import io.sfk.studyolle.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
public class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("user")
    public void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account user = accountRepository.findByNickname("user");
        isAccepted(user, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중(이미 인원이 꽉차서)")
    @WithAccount("user")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account testAccount = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", testAccount);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, testAccount);

        Account test1 = accountFactory.createAccount("test1");
        Account test2 = accountFactory.createAccount("test2");
        eventService.newEnrollment(event, test1);
        eventService.newEnrollment(event, test2);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account user = accountRepository.findByNickname("user");
        isNotAccepted(user, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("user")
    void accepted_Account_cancelEnrollment_to_FCFS_event_not_Accepted() throws Exception {
        Account user = accountRepository.findByNickname("user");
        Account test = accountFactory.createAccount("test");
        Account test1 = accountFactory. createAccount("test1");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test);

        eventService.newEnrollment(event, test1);
        eventService.newEnrollment(event, user);
        eventService.newEnrollment(event, test);

        isAccepted(test1, event);
        isAccepted(user, event);
        isNotAccepted(test, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(test1, event);
        isAccepted(test, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, user));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("user")
    void not_accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account user = accountRepository.findByNickname("user");
        Account test1 = accountFactory.createAccount("test1");
        Account test2 = accountFactory.createAccount("test2");
        Study study = studyFactory.createStudy("test-study", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        eventService.newEnrollment(event, test2);
        eventService.newEnrollment(event, test1);
        eventService.newEnrollment(event, user);

        isAccepted(test1, event);
        isAccepted(test2, event);
        isNotAccepted(user, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(test1, event);
        isAccepted(test2, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, user));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("user")
    void newEnrollment_to_CONFIRMATIVE_event_not_accepted() throws Exception {
        Account test = accountFactory.createAccount("test");
        Study study = studyFactory.createStudy("test-study", test);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, test);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account user = accountRepository.findByNickname("user");
        isNotAccepted(user, event);
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }
}
