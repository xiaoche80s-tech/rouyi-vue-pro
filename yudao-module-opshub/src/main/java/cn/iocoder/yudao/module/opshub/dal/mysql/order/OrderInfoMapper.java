package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.OrderInfoPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderInfoDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderInfoMapper extends BaseMapperX<OrderInfoDO> {

    default PageResult<OrderInfoDO> selectPage(OrderInfoPageReqVO reqVO) {
        LambdaQueryWrapperX<OrderInfoDO> wrapper = new LambdaQueryWrapperX<OrderInfoDO>()
                .eqIfPresent(OrderInfoDO::getProgressStatus, reqVO.getProgressStatus())
                .eqIfPresent(OrderInfoDO::getPayStatus, reqVO.getPayStatus())
                .eqIfPresent(OrderInfoDO::getInvStatus, reqVO.getInvStatus())
                .inIfPresent(OrderInfoDO::getProductLineCode, reqVO.getProductLineCodes())
                .inIfPresent(OrderInfoDO::getDealerCode, reqVO.getDealerCodes())
                .orderByDesc(OrderInfoDO::getId);

        // 快捷时间过滤
        applyQuickTimeFilter(wrapper, reqVO.getQuickTime());
        // 季度/月度过滤
        applyTimeDimensionFilter(wrapper, reqVO);
        // 关键词搜索
        applyKeywordFilter(wrapper, reqVO.getKeyword());

        return selectPage(reqVO, wrapper);
    }

    /**
     * 统计：分别按 progress_status / pay_status / inv_status GROUP BY
     */
    default List<Map<String, Object>> selectStatistics() {
        return selectMaps(new LambdaQueryWrapper<OrderInfoDO>()
                .select(OrderInfoDO::getProgressStatus, OrderInfoDO::getPayStatus, OrderInfoDO::getInvStatus)
                .groupBy(OrderInfoDO::getProgressStatus, OrderInfoDO::getPayStatus, OrderInfoDO::getInvStatus));
    }

    /**
     * 查询指定年份+月日格式的最大序号（用于编码自动生成）
     */
    default Integer selectMaxSeq(int year, String monthDay) {
        String prefix = "ORD-" + year + "-" + monthDay + "-";
        List<OrderInfoDO> list = selectList(new LambdaQueryWrapper<OrderInfoDO>()
                .likeRight(OrderInfoDO::getOrderCode, prefix)
                .orderByDesc(OrderInfoDO::getOrderCode));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        String maxCode = list.get(0).getOrderCode();
        String seqStr = maxCode.substring(maxCode.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 汇总金额
     */
    default BigDecimal selectTotalAmount(OrderInfoPageReqVO reqVO) {
        LambdaQueryWrapperX<OrderInfoDO> wrapper = new LambdaQueryWrapperX<OrderInfoDO>()
                .eqIfPresent(OrderInfoDO::getProgressStatus, reqVO.getProgressStatus())
                .eqIfPresent(OrderInfoDO::getPayStatus, reqVO.getPayStatus())
                .eqIfPresent(OrderInfoDO::getInvStatus, reqVO.getInvStatus())
                .inIfPresent(OrderInfoDO::getProductLineCode, reqVO.getProductLineCodes())
                .inIfPresent(OrderInfoDO::getDealerCode, reqVO.getDealerCodes());
        applyQuickTimeFilter(wrapper, reqVO.getQuickTime());
        applyTimeDimensionFilter(wrapper, reqVO);
        applyKeywordFilter(wrapper, reqVO.getKeyword());

        List<OrderInfoDO> list = selectList(wrapper);
        return list.stream()
                .map(OrderInfoDO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== 辅助方法 ==========

    private void applyQuickTimeFilter(LambdaQueryWrapperX<OrderInfoDO> wrapper, String quickTime) {
        if (StrUtil.isEmpty(quickTime) || "all".equals(quickTime)) {
            return;
        }
        LocalDate today = LocalDate.now();
        switch (quickTime) {
            case "today":
                wrapper.eq(OrderInfoDO::getOrderDate, today);
                break;
            case "week":
                wrapper.ge(OrderInfoDO::getOrderDate, today.minusDays(today.getDayOfWeek().getValue() - 1));
                break;
            case "month":
                wrapper.ge(OrderInfoDO::getOrderDate, today.withDayOfMonth(1));
                break;
            default:
                break;
        }
    }

    private void applyTimeDimensionFilter(LambdaQueryWrapperX<OrderInfoDO> wrapper, OrderInfoPageReqVO reqVO) {
        // 季度过滤
        if (CollUtil.isNotEmpty(reqVO.getQuarters())) {
            wrapper.and(w -> {
                for (int i = 0; i < reqVO.getQuarters().size(); i++) {
                    Integer quarter = reqVO.getQuarters().get(i);
                    if (i == 0) {
                        w.apply("EXTRACT(QUARTER FROM order_date) = {0}", quarter);
                    } else {
                        w.or().apply("EXTRACT(QUARTER FROM order_date) = {0}", quarter);
                    }
                }
            });
        }
        // 月度过滤
        if (CollUtil.isNotEmpty(reqVO.getMonths())) {
            wrapper.and(w -> {
                for (int i = 0; i < reqVO.getMonths().size(); i++) {
                    Integer month = reqVO.getMonths().get(i);
                    if (i == 0) {
                        w.apply("EXTRACT(MONTH FROM order_date) = {0}", month);
                    } else {
                        w.or().apply("EXTRACT(MONTH FROM order_date) = {0}", month);
                    }
                }
            });
        }
    }

    private void applyKeywordFilter(LambdaQueryWrapperX<OrderInfoDO> wrapper, String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return;
        }
        wrapper.and(w -> w
                .like(OrderInfoDO::getOrderCode, keyword)
                .or().like(OrderInfoDO::getProductLineName, keyword)
                .or().like(OrderInfoDO::getDealerName, keyword));
    }

}
