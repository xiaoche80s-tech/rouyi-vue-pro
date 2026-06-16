package cn.iocoder.yudao.module.opshub.service.aftersale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleInfoDO;

/**
 * 售后 Service 接口
 */
public interface AfterSaleInfoService {

    /**
     * 获得售后详情（含进度节点）
     */
    AfterSaleDetailRespVO getAfterSaleDetail(Long id);

    /**
     * 获得售后分页
     */
    PageResult<AfterSaleInfoDO> getAfterSalePage(AfterSaleInfoPageReqVO reqVO);

    /**
     * 获得统计数据（5 大卡片）
     */
    AfterSaleStatisticsRespVO getStatistics();

    /**
     * 更新进度节点（管理员/执行员）
     */
    void updateProgress(AfterSaleUpdateProgressReqVO reqVO);

}
