package cn.iocoder.yudao.module.opshub.dal.mysql.aftersale;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.AfterSaleInfoPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

@Mapper
public interface AfterSaleInfoMapper extends BaseMapperX<AfterSaleInfoDO> {

    default PageResult<AfterSaleInfoDO> selectPage(AfterSaleInfoPageReqVO reqVO) {
        LambdaQueryWrapperX<AfterSaleInfoDO> wrapper = new LambdaQueryWrapperX<AfterSaleInfoDO>()
                .eqIfPresent(AfterSaleInfoDO::getHandlingMethod, reqVO.getHandlingMethod())
                .eqIfPresent(AfterSaleInfoDO::getReason, reqVO.getReason())
                .eqIfPresent(AfterSaleInfoDO::getProgressStatus, reqVO.getProgressStatus())
                .inIfPresent(AfterSaleInfoDO::getProductLineCode, reqVO.getProductLineCodes())
                .inIfPresent(AfterSaleInfoDO::getDealerCode, reqVO.getDealerCodes());

        // 关键词搜索
        applyKeywordFilter(wrapper, reqVO.getKeyword());
        // 进度状态多选（未完成筛选）
        applyProgressStatusFilter(wrapper, reqVO.getProgressStatus());
        // 动态排序
        applySort(wrapper, reqVO.getSortField(), reqVO.getSortOrder());

        return selectPage(reqVO, wrapper);
    }

    // ========== 辅助方法 ==========

    private void applyKeywordFilter(LambdaQueryWrapperX<AfterSaleInfoDO> wrapper, String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return;
        }
        wrapper.and(w -> w
                .like(AfterSaleInfoDO::getAftersaleCode, keyword)
                .or().like(AfterSaleInfoDO::getOrderCode, keyword));
    }

    private void applyProgressStatusFilter(LambdaQueryWrapperX<AfterSaleInfoDO> wrapper, String progressStatus) {
        // "in_progress" 在前端可能代表"未完成"的快捷筛选，此处不特殊处理
        // 前端传具体的 progressStatus 值即可
    }

    private void applySort(LambdaQueryWrapperX<AfterSaleInfoDO> wrapper, String sortField, String sortOrder) {
        if (StrUtil.isEmpty(sortField)) {
            wrapper.orderByDesc(AfterSaleInfoDO::getId);
            return;
        }
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        switch (sortField) {
            case "aftersale_code":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getAftersaleCode);
                break;
            case "order_code":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getOrderCode);
                break;
            case "apply_time":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getApplyTime);
                break;
            case "handling_method":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getHandlingMethod);
                break;
            case "product_name":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getProductName);
                break;
            case "progress_status":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getProgressStatus);
                break;
            case "dealer_name":
                wrapper.orderBy(true, isAsc, AfterSaleInfoDO::getDealerName);
                break;
            default:
                wrapper.orderByDesc(AfterSaleInfoDO::getId);
                break;
        }
    }

}
