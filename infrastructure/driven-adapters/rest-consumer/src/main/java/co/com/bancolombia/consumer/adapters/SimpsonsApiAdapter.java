package co.com.bancolombia.consumer.adapters;

import co.com.bancolombia.consumer.RestConsumer;
import co.com.bancolombia.consumer.SimpsonsCharacterResponse;
import co.com.bancolombia.consumer.SimpsonsEpisodeResponse;
import co.com.bancolombia.consumer.SimpsonsLocationResponse;
import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class SimpsonsApiAdapter implements UserInfoGateway, SimpsonsGateway {

    private final RestConsumer client;

    @Override
    public Mono<UserInfo> getUserInfoById(Integer id) {
        return client.getCharacterById(id)
                .map(this::mapToUserInfo);
    }

    @Override
    public Mono<SimpsonsCharacter> getCharacterById(Integer id) {
        return client.getCharacterById(id)
                .map(this::mapToCharacter);
    }

    @Override
    public Mono<SimpsonsEpisode> getEpisodeById(Integer id) {
        return client.getEpisodeById(id)
                .map(this::mapToEpisode);
    }

    @Override
    public Mono<SimpsonsLocation> getLocationById(Integer id) {
        return client.getLocationById(id)
                .map(this::mapToLocation);
    }

    private UserInfo mapToUserInfo(SimpsonsCharacterResponse resp) {
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
                .firstAppearanceEp(mapEpisodeToUserInfo(resp.getFirstAppearanceEp()))
                .firstAppearanceSh(mapShortToUserInfo(resp.getFirstAppearanceSh()))
                .build();
    }

    private SimpsonsCharacter mapToCharacter(SimpsonsCharacterResponse resp) {
        return SimpsonsCharacter.builder()
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
                .build();
    }

    private SimpsonsEpisode mapToEpisode(SimpsonsEpisodeResponse resp) {
        return SimpsonsEpisode.builder()
                .id(resp.getId())
                .airdate(resp.getAirdate())
                .description(resp.getDescription())
                .episodeNumber(resp.getEpisodeNumber())
                .imagePath(resp.getImagePath())
                .name(resp.getName())
                .season(resp.getSeason())
                .synopsis(resp.getSynopsis())
                .build();
    }

    private SimpsonsLocation mapToLocation(SimpsonsLocationResponse resp) {
        return SimpsonsLocation.builder()
                .id(resp.getId())
                .name(resp.getName())
                .description(resp.getDescription())
                .build();
    }

    private UserInfo.Episode mapEpisodeToUserInfo(SimpsonsCharacterResponse.EpisodeResponse r) {
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

    private UserInfo.ShortInfo mapShortToUserInfo(SimpsonsCharacterResponse.ShortResponse r) {
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
