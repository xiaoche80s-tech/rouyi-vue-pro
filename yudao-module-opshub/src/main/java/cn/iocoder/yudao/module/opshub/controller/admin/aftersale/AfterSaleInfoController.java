package cn.iocoder.yudao.module.opshub.controller.admin.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleProgressDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.aftersale.AfterSaleProgressMapper;
import cn.iocoder.yudao.module.opshub.service.aftersale.AfterSaleInfoService;
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
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 售后模块")
@RestController
@RequestMapping("/opshub/aftersale")
@Validated
public class AfterSaleInfoController {

    @Resource
    private AfterSaleInfoService afterSaleInfoService;
    @Resource
    private AfterSaleProgressMapper afterSaleProgressMapper;

    @GetMapping("/page")
    @Operation(summary = "获得售后分页")
    @PreAuthorize("@ss.hasPermission('dealer:aftersale:query')")
    public CommonResult<PageResult<AfterSaleInfoSimpleRespVO>> getAfterSalePage(@Valid AfterSaleInfoPageReqVO pageReqVO) {
        PageResult<AfterSaleInfoDO> pageResult = afterSaleInfoService.getAfterSalePage(pageReqVO);
        // 转换为 SimpleRespVO，并附带进度节点预览
        PageResult<AfterSaleInfoSimpleRespVO> voPageResult = BeanUtils.toBean(pageResult, AfterSaleInfoSimpleRespVO.class);
        // 填充进度节点预览
        if (voPageResult.getList() != null && pageResult.getList() != null) {
            for (int i = 0; i < voPageResult.getList().size(); i++) {
                AfterSaleInfoSimpleRespVO vo = voPageResult.getList().get(i);
                AfterSaleInfoDO info = pageResult.getList().get(i);
                List<AfterSaleProgressDO> nodes = afterSaleProgressMapper.selectListByAftersaleId(info.getId());
                vo.setProgressNodes(nodes.stream().map(n -> {
                    AfterSaleInfoSimpleRespVO.ProgressNodePreview preview = new AfterSaleInfoSimpleRespVO.ProgressNodePreview();
                    preview.setNodeName(n.getNodeName());
                    preview.setNodeCode(n.getNodeCode());
                    preview.setIsCompleted(n.getIsCompleted());
                    preview.setNodeTime(n.getNodeTime());
                    return preview;
                }).collect(Collectors.toList()));
            }
        }
        return success(voPageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得售后详情（含进度节点）")
    @Parameter(name = "id", description = "售后单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:aftersale:query')")
    public CommonResult<AfterSaleDetailRespVO> getAfterSaleDetail(@RequestParam("id") Long id) {
        return success(afterSaleInfoService.getAfterSaleDetail(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得 5 大统计卡片数据")
    @PreAuthorize("@ss.hasPermission('dealer:aftersale:query')")
    public CommonResult<AfterSaleStatisticsRespVO> getStatistics() {
        return success(afterSaleInfoService.getStatistics());
    }

    @PutMapping("/update-progress")
    @Operation(summary = "更新售后进度（管理员/执行员）")
    @PreAuthorize("@ss.hasPermission('dealer:aftersale:update-progress')")
    public CommonResult<Boolean> updateProgress(@Valid @RequestBody AfterSaleUpdateProgressReqVO reqVO) {
        afterSaleInfoService.updateProgress(reqVO);
        return success(true);
    }

    @PostMapping("/batch-consult")
    @Operation(summary = "批量咨询服务（预留骨架）")
    @PreAuthorize("@ss.hasPermission('dealer:aftersale:consult')")
    public CommonResult<Boolean> batchConsult(@RequestBody List<Long> aftersaleIds) {
        // 预留骨架，后续对接客户服务模块
        return success(true);
    }

}
