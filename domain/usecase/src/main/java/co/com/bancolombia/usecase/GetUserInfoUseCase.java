package co.com.bancolombia.usecase;

import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
public record GetUserInfoUseCase(UserInfoGateway userInfoGateway) {

    public Mono<UserInfo> execute(Integer id) {
        log.info(String.format("Getting user info for id: %s", id));
        return userInfoGateway.getUserInfoById(id);
    }
}
