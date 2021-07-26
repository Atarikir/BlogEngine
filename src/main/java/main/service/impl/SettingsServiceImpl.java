package main.service.impl;

import main.api.response.SettingsResponse;
import main.repository.GlobalSettingRepository;
import main.service.SettingsService;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {

    private GlobalSettingRepository globalSettingRepository;

    public SettingsServiceImpl(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    @Override
    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();
        settingsResponse.setMultiuserMode(true);
        settingsResponse.setStatisticsIsPublic(true);
        return settingsResponse;
    }
}
