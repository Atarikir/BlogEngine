package main.service.impl;

import main.api.response.SettingsResponse;
import main.api.response.StatisticsResponse;
import main.exceptions.UnauthorizedException;
import main.model.GlobalSetting;
import main.model.Post;
import main.model.PostVote;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.model.enums.Setting;
import main.repository.GlobalSettingRepository;
import main.repository.PostRepository;
import main.repository.PostVoteRepository;
import main.repository.UserRepository;
import main.service.SettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {

    @Value("${settings.value.true}")
    private String settingValueTrue;

    @Value("${settings.value.false}")
    private String settingValueFalse;

    private final GlobalSettingRepository globalSettingRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;

    public SettingsServiceImpl(GlobalSettingRepository globalSettingRepository, UserRepository userRepository,
                               PostRepository postRepository, PostVoteRepository postVoteRepository) {
        this.globalSettingRepository = globalSettingRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
    }

    @Override
    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();

        for (GlobalSetting setting : getAllGlobalSettings()) {
            boolean value = setting.getValue().equals(settingValueTrue);
            if (setting.getCode().equals(Setting.MULTIUSER_MODE.toString()) && value) {
                settingsResponse.setMultiuserMode(true);
            }
            if (setting.getCode().equals(Setting.POST_PREMODERATION.toString()) && value) {
                settingsResponse.setPostPremoderation(true);
            }
            if (setting.getCode().equals(Setting.STATISTICS_IS_PUBLIC.toString()) && value) {
                settingsResponse.setStatisticsIsPublic(true);
            }
        }

        return settingsResponse;
    }

    @Override
    @Transactional
    public void writeGlobalSettings(SettingsResponse settingsRequest, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());

        if (user.isModerator()) {

            for (GlobalSetting globalSetting : getAllGlobalSettings()) {
                if (settingsRequest.isMultiuserMode() &&
                        globalSetting.getCode().equals(Setting.MULTIUSER_MODE.toString())) {
                    globalSetting.setValue(settingValueTrue);
                } else if (!settingsRequest.isMultiuserMode() &&
                        globalSetting.getCode().equals(Setting.MULTIUSER_MODE.toString())) {
                    globalSetting.setValue(settingValueFalse);
                }

                if (settingsRequest.isPostPremoderation() &&
                        globalSetting.getCode().equals(Setting.POST_PREMODERATION.toString())) {
                    globalSetting.setValue(settingValueTrue);
                } else if (!settingsRequest.isPostPremoderation() &&
                        globalSetting.getCode().equals(Setting.POST_PREMODERATION.toString())) {
                    globalSetting.setValue(settingValueFalse);
                }

                if (settingsRequest.isStatisticsIsPublic() &&
                        globalSetting.getCode().equals(Setting.STATISTICS_IS_PUBLIC.toString())) {
                    globalSetting.setValue(settingValueTrue);
                } else if (!settingsRequest.isStatisticsIsPublic() &&
                        globalSetting.getCode().equals(Setting.STATISTICS_IS_PUBLIC.toString())) {
                    globalSetting.setValue(settingValueFalse);
                }

                globalSettingRepository.save(globalSetting);
            }
        }
    }

    @Override
    public StatisticsResponse getMyStats(Principal principal) {
        final byte IS_ACTIVE = 1;
        User user = userRepository.findByEmail(principal.getName());
        LocalDateTime timeOfFirstPost = postRepository.getMyFirstPublicationTime(user);
        List<Post> postList = postRepository.findPostsByIsActiveAndModerationStatusAndUser(IS_ACTIVE,
                ModerationStatus.ACCEPTED, user);

        long postCount = postRepository.countByUser(user);
        long likesCount = getCountVotes(postList, (byte) 1);
        long dislikesCount = getCountVotes(postList, (byte) -1);
        long viewsCount = postRepository.getMyViewsCount(user).orElse(0L);
        long firstPublication = timeOfFirstPost == null ? 0 : timeOfFirstPost.toEpochSecond(ZoneOffset.UTC);

        return getStatisticsResponse(postCount, likesCount, dislikesCount, viewsCount, firstPublication);
    }

    @Override
    public StatisticsResponse getStatisticsAllPosts(Principal principal) {
        GlobalSetting globalSetting = globalSettingRepository.findByCode(Setting.STATISTICS_IS_PUBLIC.toString());
        LocalDateTime timeOfFirstPost = postRepository.getFirstPublicationTime();

        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            if (globalSetting.getValue().equals(settingValueFalse) && !user.isModerator()) {
                throw new UnauthorizedException();
            }
        }

        long postCount = postRepository.count();
        long likesCount = postVoteRepository.countAllByValue((byte) 1);
        long dislikesCount = postVoteRepository.countAllByValue((byte) -1);
        long viewsCount = postRepository.getAllViewsCount().orElse(0L);
        long firstPublication = timeOfFirstPost == null ? 0 : timeOfFirstPost.toEpochSecond(ZoneOffset.UTC);

        return getStatisticsResponse(postCount, likesCount, dislikesCount, viewsCount, firstPublication);

    }

    private long getCountVotes(List<Post> postList, byte voteValue){
        return postList.stream()
                .map(Post::getPostVotes)
                .map(postVotes -> postVotes.stream()
                        .map(PostVote::getValue)
                        .filter(v -> v == voteValue).count())
                .reduce(0L, Long::sum);
    }

    private StatisticsResponse getStatisticsResponse(long postCount, long likesCount, long dislikesCount,
                                                     long viewsCount, long firstPublication) {

        return StatisticsResponse.builder()
                .postsCount(postCount)
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .viewsCount(viewsCount)
                .firstPublication(firstPublication)
                .build();
    }

    private List<GlobalSetting> getAllGlobalSettings() {
        return globalSettingRepository.findAll();
    }
}