package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessagePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessageRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessageSendReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsMessageDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 咨询消息")
@RestController
@RequestMapping("/opshub/cs-message")
@Validated
public class CsMessageController {

    @Resource
    private CsMessageService csMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送咨询消息")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:reply')")
    public CommonResult<CsMessageRespVO> sendMessage(@Valid @RequestBody CsMessageSendReqVO reqVO) {
        CsMessageDO message = csMessageService.sendMessage(reqVO);
        return success(BeanUtils.toBean(message, CsMessageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得消息分页列表")
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:query')")
    public CommonResult<PageResult<CsMessageRespVO>> getMessagePage(@Valid CsMessagePageReqVO pageReqVO) {
        PageResult<CsMessageDO> pageResult = csMessageService.getMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CsMessageRespVO.class));
    }

    @PostMapping("/mark-read")
    @Operation(summary = "标记已读")
    @Parameter(name = "sessionId", description = "会话ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-consult:query')")
    public CommonResult<Boolean> markRead(@RequestParam("sessionId") Long sessionId) {
        csMessageService.markRead(sessionId);
        return success(true);
    }

}
