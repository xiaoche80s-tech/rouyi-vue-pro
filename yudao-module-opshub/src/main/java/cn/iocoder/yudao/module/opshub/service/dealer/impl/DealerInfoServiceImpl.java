package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoPageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoSaveReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerInfoMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 经销商 Service 实现类
 */
@Service
@Validated
public class DealerInfoServiceImpl implements DealerInfoService {

    @Resource
    private DealerInfoMapper dealerInfoMapper;

    @Override
    public Long createDealer(DealerInfoSaveReqVO createReqVO) {
        // 1. 校验 dealerCode 唯一
        validateDealerCodeUnique(null, createReqVO.getDealerCode());
        // 2. 转换 VO → DO
        DealerInfoDO dealer = BeanUtils.toBean(createReqVO, DealerInfoDO.class);
        // 3. 插入数据库
        dealerInfoMapper.insert(dealer);
        // 4. 返回 ID
        return dealer.getId();
    }

    @Override
    public void updateDealer(DealerInfoSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateDealerExists(updateReqVO.getId());
        validateDealerCodeUnique(updateReqVO.getId(), updateReqVO.getDealerCode());
        // 2. 转换 VO → DO
        DealerInfoDO updateObj = BeanUtils.toBean(updateReqVO, DealerInfoDO.class);
        // 3. 更新数据库
        dealerInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteDealer(Long id) {
        // 1. 校验存在
        validateDealerExists(id);
        // 2. 删除
        dealerInfoMapper.deleteById(id);
    }

    @Override
    public DealerInfoDO getDealer(Long id) {
        return dealerInfoMapper.selectById(id);
    }

    @Override
    public PageResult<DealerInfoDO> getDealerPage(DealerInfoPageReqVO pageReqVO) {
        return dealerInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DealerInfoDO> getSimpleList() {
        return dealerInfoMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<DealerInfoDO> getDealerList() {
        return dealerInfoMapper.selectList();
    }

    // ========== 校验方法 ==========

    private void validateDealerExists(Long id) {
        if (dealerInfoMapper.selectById(id) == null) {
            throw exception(DEALER_NOT_EXISTS);
        }
    }

    private void validateDealerCodeUnique(Long id, String dealerCode) {
        DealerInfoDO existing = dealerInfoMapper.selectByDealerCode(dealerCode);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(DEALER_CODE_DUPLICATE);
        }
    }

}
