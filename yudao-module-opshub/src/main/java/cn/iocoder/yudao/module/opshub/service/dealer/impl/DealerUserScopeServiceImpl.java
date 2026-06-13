package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerUserScopeDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerUserScopeMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerUserScopeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * 用户-经销商授权 Service 实现类
 */
@Service
@Validated
public class DealerUserScopeServiceImpl implements DealerUserScopeService {

    @Resource
    private DealerUserScopeMapper userScopeMapper;

    @Override
    public Set<Long> getDealerIdsByUserId(Long userId) {
        return userScopeMapper.selectDealerIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long userId, Set<Long> dealerIds) {
        // 1. 先删除旧的授权
        userScopeMapper.deleteByUserId(userId);
        // 2. 批量插入新的授权
        if (dealerIds != null && !dealerIds.isEmpty()) {
            for (Long dealerId : dealerIds) {
                DealerUserScopeDO scope = new DealerUserScopeDO();
                scope.setUserId(userId);
                scope.setDealerId(dealerId);
                userScopeMapper.insert(scope);
            }
        }
    }

    @Override
    public Set<Long> getUserIdsByDealerId(Long dealerId) {
        return userScopeMapper.selectUserIdsByDealerId(dealerId);
    }

}
