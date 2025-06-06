package io.sfk.studyolle.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sfk.studyolle.infra.AbstractContainerBaseTest;
import io.sfk.studyolle.infra.MockMvcTest;
import io.sfk.studyolle.modules.tag.Tag;
import io.sfk.studyolle.modules.zone.Zone;
import io.sfk.studyolle.modules.tag.TagForm;
import io.sfk.studyolle.modules.zone.ZoneForm;
import io.sfk.studyolle.modules.tag.TagRepository;
import io.sfk.studyolle.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static io.sfk.studyolle.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder()
            .city("test")
            .localNameOfCity("테스티시")
            .province("테스트주")
            .build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
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
    @DisplayName("프로필 수정 폼")
    @WithAccount("testUser")
    void updateProfile() throws Exception {

        String settingsProfileUrl = ROOT + SETTINGS + PROFILE;
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
        String settingsProfileURL = ROOT + SETTINGS + PROFILE;
        String bio = "긴소개----------------------------------------------------------------------------------------------------------------------------------";
        mockMvc.perform(post(settingsProfileURL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account loginAccount = accountRepository.findByNickname("testUser");
        assertNull(loginAccount.getBio());
    }

    @Test
    @WithAccount("testUser")
    @DisplayName("패스워드 수정 폼")
    void updatePassword_Form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @WithAccount("testUser")
    @DisplayName("패스워드 수정 - 입력값 정상")
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account findUser = accountRepository.findByNickname("testUser");
        assertTrue(passwordEncoder.matches("12345678", findUser.getPassword()));
    }

    @Test
    @WithAccount("testUser")
    @DisplayName("패스워드수정 - 입력값 에러 - 패스워드 불일치")
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @WithAccount("testUser")
    @DisplayName("닉네임 수정 폼")
    void updateAccountForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("닉네임 수정하기 - 입력값 정상")
    void updateAccount_success() throws Exception {
        String newNickname = "new_nickname";

        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname(newNickname));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("닉네임 수정하기 - 입력값 에러")
    void updateAccount_failure() throws Exception {
        String newNickname = "|failure|";

        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정의 태그 수정 폼")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정에 태그 추가")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account testUser = accountRepository.findByNickname("test_user");
        assertTrue(testUser.getTags().contains(newTag));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정에 태그 삭제")
    void removeTag() throws Exception {
        Account testUser = accountRepository.findByNickname("test_user");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(testUser, newTag);

        assertTrue(testUser.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(testUser.getTags().contains(newTag));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정의 지역 정보 수정 폼")
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정의 지역 정보 추가")
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Account testUser = accountRepository.findByNickname("test_user");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(testUser.getZones().contains(zone));
    }

    @Test
    @WithAccount("test_user")
    @DisplayName("계정의 지역 정보 삭제")
    void removeZone() throws Exception {
        Account testUser = accountRepository.findByNickname("test_user");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(testUser, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(testUser.getZones().contains(zone));
    }
}