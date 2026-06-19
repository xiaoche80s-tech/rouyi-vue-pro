package cn.iocoder.yudao.module.opshub.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dashboard.vo.DashboardRespVO;
import cn.iocoder.yudao.module.opshub.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 仪表盘")
@RestController
@RequestMapping("/opshub/dashboard")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "获取角色化仪表盘数据")
    public CommonResult<DashboardRespVO> getDashboard() {
        return success(dashboardService.getDashboard());
    }

}
