package co.com.bancolombia.consumer.adapters;

import co.com.bancolombia.consumer.RestConsumer;
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
        return client.getCharacterById(Integer.parseInt(String.valueOf(id)))
                .map(resp -> UserInfo.builder()
                        .id(Integer.valueOf(resp.getId().toString()))
                        .name(resp.getName())
                        .description(resp.getDescription())
                        .birthdate(resp.getBirthdate())
                        .status(resp.getStatus())
                        .build());
    }
}
