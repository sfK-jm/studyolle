package io.sfk.studyolle.modules.study;

import io.sfk.studyolle.infra.MockMvcTest;
import io.sfk.studyolle.modules.account.AccountFactory;
import io.sfk.studyolle.modules.account.AccountRepository;
import io.sfk.studyolle.modules.account.WithAccount;
import io.sfk.studyolle.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;

    @Test
    @WithAccount("test_user")
    @DisplayName("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account tUser = accountFactory.createAccount("t_user");
        Study study = studyFactory.createStudy("test-study", tUser);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionFrom() throws Exception {
        Account testUser = accountRepository.findByNickname("test_user");
        Study study = studyFactory.createStudy("test-study", testUser);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account testUser = accountRepository.findByNickname("test_user");
        Study study = studyFactory.createStudy("test-study", testUser);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";

        mockMvc.perform(post(settingsDescriptionUrl)
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception{
        Account testUser = accountRepository.findByNickname("test_user");
        Study study = studyFactory.createStudy("test-study", testUser);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";

        mockMvc.perform(post(settingsDescriptionUrl)
                        .param("shortDescription", "")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }

}