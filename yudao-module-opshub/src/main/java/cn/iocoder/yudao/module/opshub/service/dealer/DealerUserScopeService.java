package cn.iocoder.yudao.module.opshub.service.dealer;

import java.util.List;
import java.util.Set;

/**
 * 用户-经销商授权 Service 接口
 */
public interface DealerUserScopeService {

    /**
     * 获取用户授权的经销商 Code 集合
     */
    Set<String> getDealerCodesByUserId(Long userId);

    /**
     * 分配用户经销商授权（全量替换）
     */
    void assign(Long userId, Set<String> dealerCodes);

    /**
     * 获取经销商关联的所有代理人 userId 集合
     */
    Set<Long> getUserIdsByDealerCode(String dealerCode);

    /**
     * 获取所有有经销商授权的用户及其授权编码
     *
     * @return 用户授权范围列表
     */
    List<UserScopeDTO> getAllUserDealerScopes();

}
