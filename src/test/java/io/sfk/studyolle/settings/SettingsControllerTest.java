package io.sfk.studyolle.settings;

import io.sfk.studyolle.WithAccount;
import io.sfk.studyolle.account.AccountRepository;
import io.sfk.studyolle.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정하기 폼")
    @WithAccount("testUser")
    public void updateProfileForm() throws Exception {
        String settingsProfileURL = "/settings/profile";

        mockMvc.perform(get(settingsProfileURL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    @DisplayName("프로플 수정 폼")
    @WithAccount("testUser")
    void updateProfile() throws Exception {

        String settingsProfileUrl = SettingsController.SETTINGS_PROFILE_URL;
        String bio = "짧은 소개";

        mockMvc.perform(post(settingsProfileUrl)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsProfileUrl))
                .andExpect(flash().attributeExists("message"));

        Account loginAccount = accountRepository.findByNickname("testUser");
        assertEquals(bio, loginAccount.getBio());
    }

    @DisplayName("프로필 수정하기 - 입력값 너무 길게")
    @WithAccount("testUser")
    @Test
    public void updateProfileTooLongBio() throws Exception {
        String settingsProfileURL = "/settings/profile";
        String bio = "긴소개----------------------------------------------------------------------------------------------------------------------------------";
        mockMvc.perform(post(settingsProfileURL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account loginAccount = accountRepository.findByNickname("testUser");
        assertNull(loginAccount.getBio());
    }

}