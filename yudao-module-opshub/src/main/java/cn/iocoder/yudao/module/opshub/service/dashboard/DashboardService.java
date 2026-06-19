package cn.iocoder.yudao.module.opshub.service.dashboard;

import cn.iocoder.yudao.module.opshub.controller.admin.dashboard.vo.DashboardRespVO;

/**
 * 仪表盘 Service 接口
 */
public interface DashboardService {

    /**
     * 获取当前登录用户的角色化仪表盘数据
     */
    DashboardRespVO getDashboard();

}
