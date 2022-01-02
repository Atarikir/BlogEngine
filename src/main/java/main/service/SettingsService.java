package main.service;

import main.api.response.SettingsResponse;
import main.api.response.StatisticsResponse;

import java.security.Principal;


public interface SettingsService {
    SettingsResponse getGlobalSettings();

    void writeGlobalSettings(SettingsResponse settingsResponse, Principal principal);

    StatisticsResponse getStatisticsAllPosts(Principal principal);

    StatisticsResponse getMyStats(Principal principal);
}
