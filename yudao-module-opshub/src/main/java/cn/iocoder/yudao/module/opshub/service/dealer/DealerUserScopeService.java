package cn.iocoder.yudao.module.opshub.service.dealer;

import java.util.Set;

/**
 * 用户-经销商授权 Service 接口
 */
public interface DealerUserScopeService {

    /**
     * 获取用户授权的经销商 ID 集合
     */
    Set<Long> getDealerIdsByUserId(Long userId);

    /**
     * 分配用户经销商授权（全量替换）
     */
    void assign(Long userId, Set<Long> dealerIds);

    /**
     * 获取经销商关联的所有代理人 userId 集合
     */
    Set<Long> getUserIdsByDealerId(Long dealerId);

}
