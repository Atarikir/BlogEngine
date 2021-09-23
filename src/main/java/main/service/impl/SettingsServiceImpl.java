package main.service.impl;

import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
import main.repository.GlobalSettingRepository;
import main.service.SettingsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final GlobalSettingRepository globalSettingRepository;

    public SettingsServiceImpl(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    @Override
    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();

        List<GlobalSetting> globalSettings = globalSettingRepository.findAll();

        for (GlobalSetting setting : globalSettings) {
            boolean value = setting.getValue().equals("YES");
            if (setting.getCode().equals("MULTIUSER_MODE") && value) {
                settingsResponse.setMultiuserMode(true);
            }
            if (setting.getCode().equals("POST_PREMODERATION") && value) {
                settingsResponse.setPostPremoderation(true);
            }
            if (setting.getCode().equals("STATISTICS_IS_PUBLIC") && value) {
                settingsResponse.setStatisticsIsPublic(true);
            }
        }

        return settingsResponse;
    }
}
