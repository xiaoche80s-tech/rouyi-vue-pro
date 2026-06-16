package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsMessageDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsSessionDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsMessageService;
import cn.iocoder.yudao.module.opshub.service.cs.CsSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 咨询会话")
@RestController
@RequestMapping("/opshub/cs-session")
@Validated
public class CsSessionController {

    @Resource
    private CsSessionService csSessionService;

    @Resource
    private CsMessageService csMessageService;

    @PostMapping("/create")
    @Operation(summary = "创建咨询会话")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:create')")
    public CommonResult<Long> createSession(@Valid @RequestBody CsSessionCreateReqVO reqVO) {
        return success(csSessionService.createSession(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得咨询会话分页")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:query')")
    public CommonResult<PageResult<CsSessionRespVO>> getSessionPage(@Valid CsSessionPageReqVO pageReqVO) {
        PageResult<CsSessionDO> pageResult = csSessionService.getSessionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CsSessionRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得咨询会话详情（含历史消息）")
    @Parameter(name = "id", description = "会话ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:query')")
    public CommonResult<CsSessionDetailRespVO> getSession(@RequestParam("id") Long id) {
        CsSessionDO session = csSessionService.getSession(id);
        CsSessionDetailRespVO respVO = BeanUtils.toBean(session, CsSessionDetailRespVO.class);
        // 加载历史消息
        List<CsMessageDO> messages = csMessageService.getMessageList(id);
        respVO.setMessages(BeanUtils.toBean(messages, CsMessageRespVO.class));
        return success(respVO);
    }

    @PostMapping("/accept")
    @Operation(summary = "执行员接单")
    @Parameter(name = "id", description = "会话ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:reply')")
    public CommonResult<Boolean> acceptSession(@RequestParam("id") Long id) {
        csSessionService.acceptSession(id);
        return success(true);
    }

    @PostMapping("/complete")
    @Operation(summary = "执行员完成处理")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:complete')")
    public CommonResult<Boolean> completeSession(@Valid @RequestBody CsSessionCompleteReqVO reqVO) {
        csSessionService.completeSession(reqVO);
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "经销商关闭对话")
    @Parameter(name = "id", description = "会话ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:close')")
    public CommonResult<Boolean> closeSession(@RequestParam("id") Long id) {
        csSessionService.closeSession(id);
        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得咨询统计")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:query')")
    public CommonResult<CsConsultStatisticsRespVO> getStatistics() {
        return success(csSessionService.getStatistics());
    }

}
