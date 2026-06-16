package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsOpReqDO;

/**
 * 操作请求 Service 接口
 */
public interface CsOpReqService {

    /**
     * 创建操作请求
     * 自动编号，状态=待处理
     */
    Long createOpReq(CsOpReqCreateReqVO reqVO);

    /**
     * 获取操作请求详情
     */
    CsOpReqDO getOpReq(Long id);

    /**
     * 获取操作请求分页（含角色可见性过滤）
     */
    PageResult<CsOpReqDO> getOpReqPage(CsOpReqPageReqVO reqVO);

    /**
     * 执行员接单
     * 状态：待处理(0) → 处理中(1)
     */
    void acceptOpReq(Long id);

    /**
     * 执行员提交结果
     * 状态：处理中(1) → 等待验收(2)
     */
    void submitOpReq(CsOpReqSubmitReqVO reqVO);

    /**
     * 经销商验收
     * 状态：等待验收(2) → 已完成(3)
     */
    void verifyOpReq(Long id);

}
