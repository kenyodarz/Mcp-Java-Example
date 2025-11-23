package co.com.bancolombia.consumer.adapters;

import co.com.bancolombia.consumer.RestConsumer;
import co.com.bancolombia.consumer.SimpsonsCharacterResponse;
import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class SimpsonsApiAdapter implements UserInfoGateway {

    private final RestConsumer client;

    @Override
    public Mono<UserInfo> getUserInfoById(Integer id) {
        return client.getCharacterById(id)
                .map(this::mapToDomain);
    }

    private UserInfo mapToDomain(SimpsonsCharacterResponse resp) {
        return UserInfo.builder()
                .id(resp.getId())
                .age(resp.getAge())
                .birthdate(resp.getBirthdate())
                .description(resp.getDescription())
                .gender(resp.getGender())
                .name(resp.getName())
                .occupation(resp.getOccupation())
                .phrases(resp.getPhrases())
                .portraitPath(resp.getPortraitPath())
                .status(resp.getStatus())
                .firstAppearanceEp(mapEpisode(resp.getFirstAppearanceEp()))
                .firstAppearanceSh(mapShort(resp.getFirstAppearanceSh()))
                .build();
    }

    private UserInfo.Episode mapEpisode(SimpsonsCharacterResponse.EpisodeResponse r) {
        if (r == null) {
            return null;
        }
        return UserInfo.Episode.builder()
                .id(r.getId())
                .airdate(r.getAirdate())
                .description(r.getDescription())
                .episodeNumber(r.getEpisodeNumber())
                .imagePath(r.getImagePath())
                .name(r.getName())
                .season(r.getSeason())
                .synopsis(r.getSynopsis())
                .build();
    }

    private UserInfo.ShortInfo mapShort(SimpsonsCharacterResponse.ShortResponse r) {
        if (r == null) {
            return null;
        }
        return UserInfo.ShortInfo.builder()
                .id(r.getId())
                .airdate(r.getAirdate())
                .description(r.getDescription())
                .episodeNumber(r.getEpisodeNumber())
                .imagePath(r.getImagePath())
                .name(r.getName())
                .season(r.getSeason())
                .synopsis(r.getSynopsis())
                .build();
    }
}