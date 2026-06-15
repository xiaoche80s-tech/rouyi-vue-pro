package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLinePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLineSaveReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineRelationDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerInfoMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerProductLineMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerProductLineRelationMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerProductLineService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 产品线 Service 实现类
 */
@Service
@Validated
public class DealerProductLineServiceImpl implements DealerProductLineService {

    @Resource
    private DealerProductLineMapper productLineMapper;
    @Resource
    private DealerProductLineRelationMapper productLineRelationMapper;
    @Resource
    private DealerInfoMapper dealerInfoMapper;

    @Override
    public Long createProductLine(DealerProductLineSaveReqVO createReqVO) {
        // 1. 校验 productLineCode 唯一
        validateProductLineCodeUnique(null, createReqVO.getProductLineCode());
        // 2. 转换 VO → DO
        DealerProductLineDO productLine = BeanUtils.toBean(createReqVO, DealerProductLineDO.class);
        // 3. 插入数据库
        productLineMapper.insert(productLine);
        // 4. 返回 ID
        return productLine.getId();
    }

    @Override
    public void updateProductLine(DealerProductLineSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateProductLineExists(updateReqVO.getId());
        validateProductLineCodeUnique(updateReqVO.getId(), updateReqVO.getProductLineCode());
        // 2. 转换 VO → DO
        DealerProductLineDO updateObj = BeanUtils.toBean(updateReqVO, DealerProductLineDO.class);
        // 3. 更新数据库
        productLineMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductLine(Long id) {
        // 1. 校验存在
        validateProductLineExists(id);
        // 2. 删除
        productLineMapper.deleteById(id);
    }

    @Override
    public DealerProductLineDO getProductLine(Long id) {
        return productLineMapper.selectById(id);
    }

    @Override
    public PageResult<DealerProductLineDO> getProductLinePage(DealerProductLinePageReqVO pageReqVO) {
        return productLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DealerProductLineDO> getSimpleList() {
        return productLineMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    // ========== 经销商绑定 ==========

    @Override
    public void bindDealer(String productLineCode, String dealerCode) {
        // 1. 校验产品线存在
        DealerProductLineDO productLine = productLineMapper.selectByProductLineCode(productLineCode);
        if (productLine == null) {
            throw exception(PRODUCT_LINE_NOT_EXISTS);
        }
        // 2. 校验经销商存在
        DealerInfoDO dealer = dealerInfoMapper.selectByDealerCode(dealerCode);
        if (dealer == null) {
            throw exception(DEALER_NOT_EXISTS);
        }
        // 3. 校验未重复绑定
        if (productLineRelationMapper.selectByDealerCodeAndProductLineCode(dealerCode, productLineCode) != null) {
            throw exception(DEALER_PRODUCT_LINE_EXISTS);
        }
        // 4. 插入关联
        DealerProductLineRelationDO relation = new DealerProductLineRelationDO();
        relation.setProductLineCode(productLineCode);
        relation.setDealerCode(dealerCode);
        productLineRelationMapper.insert(relation);
    }

    @Override
    public void unbindDealer(String productLineCode, String dealerCode) {
        productLineRelationMapper.deleteByDealerCodeAndProductLineCode(dealerCode, productLineCode);
    }

    @Override
    public List<DealerInfoDO> getDealers(String productLineCode) {
        List<DealerProductLineRelationDO> relations = productLineRelationMapper.selectListByProductLineCode(productLineCode);
        if (relations.isEmpty()) {
            return List.of();
        }
        List<String> dealerCodes = relations.stream()
                .map(DealerProductLineRelationDO::getDealerCode)
                .collect(Collectors.toList());
        // 通过 dealerCode 查询经销商信息
        return dealerCodes.stream()
                .map(code -> dealerInfoMapper.selectOne(DealerInfoDO::getDealerCode, code))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ========== 校验方法 ==========

    private void validateProductLineExists(Long id) {
        if (productLineMapper.selectById(id) == null) {
            throw exception(PRODUCT_LINE_NOT_EXISTS);
        }
    }

    private void validateProductLineCodeUnique(Long id, String productLineCode) {
        DealerProductLineDO existing = productLineMapper.selectByProductLineCode(productLineCode);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(PRODUCT_LINE_CODE_DUPLICATE);
        }
    }

}
