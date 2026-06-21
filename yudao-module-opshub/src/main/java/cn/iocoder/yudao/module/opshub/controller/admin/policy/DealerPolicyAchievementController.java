package cn.iocoder.yudao.module.opshub.controller.admin.policy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.DealerPolicyAchievementPageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.DealerPolicyAchievementSaveReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyAchievementDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyAchievementMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 政策达成明细")
@RestController
@RequestMapping("/opshub/dealer-policy-achievement")
@Validated
public class DealerPolicyAchievementController {

    @Resource
    private DealerPolicyAchievementMapper achievementMapper;

    @PostMapping("/create")
    @Operation(summary = "创建达成明细")
    @PreAuthorize("@ss.hasPermission('dealer:policy:create')")
    public CommonResult<Long> createAchievement(@Valid @RequestBody DealerPolicyAchievementSaveReqVO reqVO) {
        DealerPolicyAchievementDO doObj = BeanUtils.toBean(reqVO, DealerPolicyAchievementDO.class);
        achievementMapper.insert(doObj);
        return success(doObj.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新达成明细")
    @PreAuthorize("@ss.hasPermission('dealer:policy:update')")
    public CommonResult<Boolean> updateAchievement(@Valid @RequestBody DealerPolicyAchievementSaveReqVO reqVO) {
        DealerPolicyAchievementDO doObj = BeanUtils.toBean(reqVO, DealerPolicyAchievementDO.class);
        achievementMapper.updateById(doObj);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除达成明细")
    @Parameter(name = "id", description = "达成明细ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:policy:delete')")
    public CommonResult<Boolean> deleteAchievement(@RequestParam("id") Long id) {
        achievementMapper.deleteById(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得达成明细分页")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<PageResult<DealerPolicyAchievementDO>> getAchievementPage(@Valid DealerPolicyAchievementPageReqVO pageReqVO) {
        return success(achievementMapper.selectPage(pageReqVO));
    }

}
