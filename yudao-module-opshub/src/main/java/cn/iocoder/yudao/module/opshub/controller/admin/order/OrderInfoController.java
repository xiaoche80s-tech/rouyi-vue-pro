package cn.iocoder.yudao.module.opshub.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderInfoDO;
import cn.iocoder.yudao.module.opshub.service.order.OrderInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订单模块")
@RestController
@RequestMapping("/opshub/order")
@Validated
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    // ========== Task 3：4 个基础 API ==========

    @GetMapping("/page")
    @Operation(summary = "获得订单分页")
    @PreAuthorize("@ss.hasPermission('dealer:order:query')")
    public CommonResult<PageResult<OrderInfoSimpleRespVO>> getOrderPage(@Valid OrderInfoPageReqVO pageReqVO) {
        PageResult<OrderInfoDO> pageResult = orderInfoService.getOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OrderInfoSimpleRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得订单详情（含 6 Tab 数据）")
    @Parameter(name = "id", description = "订单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:order:query')")
    public CommonResult<OrderDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        return success(orderInfoService.getOrderDetail(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得 6 大统计卡片数据")
    @PreAuthorize("@ss.hasPermission('dealer:order:query')")
    public CommonResult<OrderStatisticsRespVO> getStatistics() {
        return success(orderInfoService.getStatistics());
    }

    @PutMapping("/update-progress")
    @Operation(summary = "更新订单进度（管理员/执行员）")
    @PreAuthorize("@ss.hasPermission('dealer:order:update')")
    public CommonResult<Boolean> updateProgress(@RequestParam("id") Long id,
                                                 @RequestParam("progressStatus") String progressStatus) {
        orderInfoService.updateProgress(id, progressStatus);
        return success(true);
    }

    // ========== Task 4：8 个业务操作 API ==========

    @PostMapping("/apply-payment")
    @Operation(summary = "申请付款")
    @PreAuthorize("@ss.hasPermission('dealer:order:pay')")
    public CommonResult<Boolean> applyPayment(@Valid @RequestBody OrderApplyPaymentReqVO reqVO) {
        orderInfoService.applyPayment(reqVO.getOrderId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/apply-invoice")
    @Operation(summary = "申请开票")
    @PreAuthorize("@ss.hasPermission('dealer:order:invoice')")
    public CommonResult<Boolean> applyInvoice(@Valid @RequestBody OrderApplyInvoiceReqVO reqVO) {
        orderInfoService.applyInvoice(reqVO.getOrderId(), reqVO.getCompanyName(),
                reqVO.getTaxNo(), reqVO.getSpecialRequest());
        return success(true);
    }

    @PostMapping("/apply-return")
    @Operation(summary = "申请退货")
    @PreAuthorize("@ss.hasPermission('dealer:order:return')")
    public CommonResult<Boolean> applyReturn(@Valid @RequestBody OrderApplyReturnReqVO reqVO) {
        orderInfoService.applyReturn(reqVO);
        return success(true);
    }

    @PostMapping("/batch-apply-payment")
    @Operation(summary = "批量申请付款")
    @PreAuthorize("@ss.hasPermission('dealer:order:pay')")
    public CommonResult<Map<String, Object>> batchApplyPayment(@RequestBody List<Long> orderIds) {
        return success(batchOperation(orderIds, (id) -> {
            orderInfoService.applyPayment(id, null);
            return null;
        }));
    }

    @PostMapping("/batch-apply-invoice")
    @Operation(summary = "批量申请开票")
    @PreAuthorize("@ss.hasPermission('dealer:order:invoice')")
    public CommonResult<Map<String, Object>> batchApplyInvoice(@RequestBody List<Long> orderIds) {
        return success(batchOperation(orderIds, (id) -> {
            // 批量开票使用默认抬头（后续可扩展）
            orderInfoService.applyInvoice(id, "", "", null);
            return null;
        }));
    }

    @PostMapping("/batch-apply-return")
    @Operation(summary = "批量申请退货")
    @PreAuthorize("@ss.hasPermission('dealer:order:return')")
    public CommonResult<Map<String, Object>> batchApplyReturn(@RequestBody List<OrderApplyReturnReqVO> reqVOs) {
        List<Long> successIds = new ArrayList<>();
        Map<Long, String> failMap = new HashMap<>();
        for (OrderApplyReturnReqVO reqVO : reqVOs) {
            try {
                orderInfoService.applyReturn(reqVO);
                successIds.add(reqVO.getOrderId());
            } catch (Exception e) {
                failMap.put(reqVO.getOrderId(), e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successIds", successIds);
        result.put("failures", failMap);
        result.put("successCount", successIds.size());
        result.put("failCount", failMap.size());
        return success(result);
    }

    @PostMapping("/batch-consult")
    @Operation(summary = "批量咨询服务（预留骨架）")
    @PreAuthorize("@ss.hasPermission('dealer:order:consult')")
    public CommonResult<Boolean> batchConsult(@RequestBody List<Long> orderIds) {
        // 预留骨架，后续对接客户服务模块
        return success(true);
    }

    // ========== 辅助方法 ==========

    /**
     * 批量操作通用方法
     */
    private Map<String, Object> batchOperation(List<Long> orderIds, java.util.function.Function<Long, Void> action) {
        List<Long> successIds = new ArrayList<>();
        Map<Long, String> failMap = new HashMap<>();
        for (Long id : orderIds) {
            try {
                action.apply(id);
                successIds.add(id);
            } catch (Exception e) {
                failMap.put(id, e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successIds", successIds);
        result.put("failures", failMap);
        result.put("successCount", successIds.size());
        result.put("failCount", failMap.size());
        return result;
    }

}
