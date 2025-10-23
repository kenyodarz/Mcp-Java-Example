package co.com.bancolombia.model.userinfo.gateways;

import co.com.bancolombia.model.userinfo.UserInfo;
import reactor.core.publisher.Mono;

public interface UserInfoGateway {

    Mono<UserInfo> getUserInfoById(Integer id);
}
