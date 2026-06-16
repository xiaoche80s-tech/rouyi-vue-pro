package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsAttachmentRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsAttachmentUploadReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsAttachmentDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 通用附件")
@RestController
@RequestMapping("/opshub/cs-attachment")
@Validated
public class CsAttachmentController {

    @Resource
    private CsAttachmentService csAttachmentService;

    @PostMapping("/upload")
    @Operation(summary = "上传附件")
    public CommonResult<Long> uploadAttachment(@Valid @RequestBody CsAttachmentUploadReqVO reqVO) {
        return success(csAttachmentService.uploadAttachment(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "查询附件列表")
    @Parameters({
            @Parameter(name = "module", description = "模块", required = true),
            @Parameter(name = "businessId", description = "业务ID", required = true)
    })
    public CommonResult<List<CsAttachmentRespVO>> getAttachmentList(
            @RequestParam("module") String module,
            @RequestParam("businessId") Long businessId) {
        List<CsAttachmentDO> list = csAttachmentService.getAttachmentList(module, businessId);
        return success(BeanUtils.toBean(list, CsAttachmentRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除附件")
    @Parameter(name = "id", description = "附件ID", required = true)
    public CommonResult<Boolean> deleteAttachment(@RequestParam("id") Long id) {
        csAttachmentService.deleteAttachment(id);
        return success(true);
    }

}
