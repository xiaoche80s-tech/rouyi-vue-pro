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
    public Set<String> getDealerCodesByUserId(Long userId) {
        return userScopeMapper.selectDealerCodesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long userId, Set<String> dealerCodes) {
        // 1. 先删除旧的授权
        userScopeMapper.deleteByUserId(userId);
        // 2. 批量插入新的授权
        if (dealerCodes != null && !dealerCodes.isEmpty()) {
            for (String dealerCode : dealerCodes) {
                DealerUserScopeDO scope = new DealerUserScopeDO();
                scope.setUserId(userId);
                scope.setDealerCode(dealerCode);
                userScopeMapper.insert(scope);
            }
        }
    }

    @Override
    public Set<Long> getUserIdsByDealerCode(String dealerCode) {
        return userScopeMapper.selectUserIdsByDealerCode(dealerCode);
    }

}
