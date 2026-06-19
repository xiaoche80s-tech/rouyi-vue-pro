package cn.iocoder.yudao.module.opshub.service.aftersale.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleProgressDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.aftersale.AfterSaleInfoMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.aftersale.AfterSaleProgressMapper;
import cn.iocoder.yudao.module.opshub.enums.AfterSaleHandlingMethodEnum;
import cn.iocoder.yudao.module.opshub.enums.AfterSaleReasonEnum;
import cn.iocoder.yudao.module.opshub.service.aftersale.AfterSaleInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 售后 Service 实现类
 */
@Service
@Validated
public class AfterSaleInfoServiceImpl implements AfterSaleInfoService {

    @Resource
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Resource
    private AfterSaleProgressMapper afterSaleProgressMapper;

    @Override
    public AfterSaleDetailRespVO getAfterSaleDetail(Long id) {
        // 1. 查询主表
        AfterSaleInfoDO info = afterSaleInfoMapper.selectById(id);
        if (info == null) {
            throw exception(AFTERSALE_NOT_EXISTS);
        }

        // 2. 查询进度节点
        List<AfterSaleProgressDO> progressNodes = afterSaleProgressMapper.selectListByAftersaleId(id);

        // 3. 组装响应
        AfterSaleDetailRespVO resp = new AfterSaleDetailRespVO();
        resp.setId(info.getId());
        resp.setAftersaleCode(info.getAftersaleCode());
        resp.setDealerName(info.getDealerName());
        resp.setProductLineName(info.getProductLineName());
        resp.setOrderCode(info.getOrderCode());
        resp.setHandlingMethod(info.getHandlingMethod());
        resp.setReason(info.getReason());
        resp.setProgressStatus(info.getProgressStatus());
        resp.setCurrentStep(info.getCurrentStep());
        resp.setProductName(info.getProductName());
        resp.setProductSpec(info.getProductSpec());
        resp.setQuantity(info.getQuantity());
        resp.setRefundAmount(info.getRefundAmount());
        resp.setRefundStatus(info.getRefundStatus());
        resp.setRedInvoiceStatus(info.getRedInvoiceStatus());
        resp.setLogisticsCompany(info.getLogisticsCompany());
        resp.setLogisticsNo(info.getLogisticsNo());
        resp.setExchangeLogisticsCompany(info.getExchangeLogisticsCompany());
        resp.setExchangeLogisticsNo(info.getExchangeLogisticsNo());
        resp.setApplyTime(info.getApplyTime());
        resp.setApprovedTime(info.getApprovedTime());
        resp.setCompletedTime(info.getCompletedTime());
        resp.setRemark(info.getRemark());

        // 枚举中文名
        AfterSaleHandlingMethodEnum methodEnum = AfterSaleHandlingMethodEnum.getByCode(info.getHandlingMethod());
        resp.setHandlingMethodName(methodEnum != null ? methodEnum.getName() : info.getHandlingMethod());
        AfterSaleReasonEnum reasonEnum = AfterSaleReasonEnum.getByCode(info.getReason());
        resp.setReasonName(reasonEnum != null ? reasonEnum.getName() : info.getReason());

        resp.setProgressNodes(progressNodes);
        return resp;
    }

    @Override
    public PageResult<AfterSaleInfoDO> getAfterSalePage(AfterSaleInfoPageReqVO reqVO) {
        return afterSaleInfoMapper.selectPage(reqVO);
    }

    @Override
    public AfterSaleStatisticsRespVO getStatistics() {
        return getStatistics(null);
    }

    @Override
    public AfterSaleStatisticsRespVO getStatistics(LocalDateTime startTime) {
        LambdaQueryWrapperX<AfterSaleInfoDO> wrapper = new LambdaQueryWrapperX<>();
        if (startTime != null) {
            wrapper.ge(AfterSaleInfoDO::getCreateTime, startTime);
        }
        List<AfterSaleInfoDO> allList = afterSaleInfoMapper.selectList(wrapper);
        int total = allList.size();

        AfterSaleStatisticsRespVO resp = new AfterSaleStatisticsRespVO();
        resp.setTotalCount(total);
        // 退款：handling_method = return_refund
        resp.setRefundCount((int) allList.stream()
                .filter(o -> "return_refund".equals(o.getHandlingMethod())).count());
        // 退货：全部 handling_method（即总数）
        resp.setReturnCount(total);
        // 已完成
        int completedCount = (int) allList.stream()
                .filter(o -> "completed".equals(o.getProgressStatus())).count();
        resp.setCompletedCount(completedCount);
        // 已完成占比
        resp.setCompletedRate(total > 0
                ? BigDecimal.valueOf(completedCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        // 未完成
        resp.setInProgressCount((int) allList.stream()
                .filter(o -> !"completed".equals(o.getProgressStatus())).count());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(AfterSaleUpdateProgressReqVO reqVO) {
        // 1. 校验售后单存在
        AfterSaleInfoDO info = afterSaleInfoMapper.selectById(reqVO.getAftersaleId());
        if (info == null) {
            throw exception(AFTERSALE_NOT_EXISTS);
        }
        // 2. 校验未完成
        if ("completed".equals(info.getProgressStatus())) {
            throw exception(AFTERSALE_ALREADY_COMPLETED);
        }
        // 3. 校验节点顺序（stepOrder == currentStep + 1）
        Integer stepOrder = reqVO.getStepOrder();
        if (stepOrder != info.getCurrentStep() + 1) {
            throw exception(AFTERSALE_PROGRESS_ORDER_ERROR);
        }

        // 4. 查找对应进度节点
        List<AfterSaleProgressDO> progressNodes = afterSaleProgressMapper.selectListByAftersaleId(info.getId());
        AfterSaleProgressDO targetNode = progressNodes.stream()
                .filter(n -> n.getSortOrder().equals(stepOrder))
                .findFirst()
                .orElseThrow(() -> exception(AFTERSALE_PROGRESS_NOT_EXISTS));

        // 5. 校验节点未完成
        if (Boolean.TRUE.equals(targetNode.getIsCompleted())) {
            throw exception(AFTERSALE_PROGRESS_ALREADY_DONE);
        }

        // 6. 更新节点
        LocalDateTime now = LocalDateTime.now();
        AfterSaleProgressDO updateNode = new AfterSaleProgressDO();
        updateNode.setId(targetNode.getId());
        updateNode.setIsCompleted(true);
        updateNode.setNodeTime(now);
        updateNode.setRemark(reqVO.getRemark());
        afterSaleProgressMapper.updateById(updateNode);

        // 7. 更新主表
        AfterSaleInfoDO updateInfo = new AfterSaleInfoDO();
        updateInfo.setId(info.getId());
        updateInfo.setCurrentStep(stepOrder);

        // 8. 特殊节点处理
        String nodeCode = targetNode.getNodeCode();
        // 商品退回节点 → 更新物流信息
        if ("returned".equals(nodeCode)) {
            if (reqVO.getLogisticsCompany() != null) {
                updateInfo.setLogisticsCompany(reqVO.getLogisticsCompany());
            }
            if (reqVO.getLogisticsNo() != null) {
                updateInfo.setLogisticsNo(reqVO.getLogisticsNo());
            }
        }
        // 换货发出节点 → 更新换货物流
        if ("exchange_sent".equals(nodeCode)) {
            if (reqVO.getLogisticsCompany() != null) {
                updateInfo.setExchangeLogisticsCompany(reqVO.getLogisticsCompany());
            }
            if (reqVO.getLogisticsNo() != null) {
                updateInfo.setExchangeLogisticsNo(reqVO.getLogisticsNo());
            }
        }
        // 退款完成节点 → 回填退款金额和退款状态
        if ("refund_done".equals(nodeCode)) {
            if (reqVO.getRefundAmount() != null) {
                updateInfo.setRefundAmount(reqVO.getRefundAmount());
            }
            updateInfo.setRefundStatus("refunded");
        }
        // 红字发票节点 → 回填红字发票状态
        if ("red_invoice".equals(nodeCode)) {
            updateInfo.setRedInvoiceStatus("issued");
        }

        // 9. 状态流转
        if (stepOrder == 2) {
            // 审核通过 → 按 handling_method 设置状态
            if ("exchange".equals(info.getHandlingMethod())) {
                updateInfo.setProgressStatus("exchanging");
            } else {
                updateInfo.setProgressStatus("in_progress");
            }
            updateInfo.setApprovedTime(now);
        }
        if (stepOrder == 5) {
            // 最后节点完成 → 已完成
            updateInfo.setProgressStatus("completed");
            updateInfo.setCompletedTime(now);
        }

        afterSaleInfoMapper.updateById(updateInfo);
    }

}
