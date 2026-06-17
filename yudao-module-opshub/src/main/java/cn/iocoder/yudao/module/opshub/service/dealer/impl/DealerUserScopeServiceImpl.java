package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerUserScopeDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerUserScopeMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerUserScopeService;
import cn.iocoder.yudao.module.opshub.service.dealer.UserScopeDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<UserScopeDTO> getAllUserDealerScopes() {
        // 查全表，按 userId 分组
        List<DealerUserScopeDO> allScopes = userScopeMapper.selectList();
        Map<Long, Set<String>> userCodeMap = allScopes.stream()
                .collect(Collectors.groupingBy(
                        DealerUserScopeDO::getUserId,
                        Collectors.mapping(DealerUserScopeDO::getDealerCode, Collectors.toSet())
                ));
        List<UserScopeDTO> result = new ArrayList<>();
        userCodeMap.forEach((userId, codes) -> result.add(new UserScopeDTO(userId, codes)));
        return result;
    }

}
