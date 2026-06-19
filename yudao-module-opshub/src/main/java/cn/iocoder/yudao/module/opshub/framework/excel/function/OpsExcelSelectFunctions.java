package cn.iocoder.yudao.module.opshub.framework.excel.function;

import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpsHub Excel 导入枚举下拉数据源
 * <p>
 * 每种 Converter 对应一个 ExcelColumnSelectFunction Bean，
 * 供 ImportExcelVO 的 @ExcelColumnSelect(functionName=xxx) 使用。
 */
@Configuration(proxyBeanMethods = false)
public class OpsExcelSelectFunctions {

    // ==================== 通用 ====================

    @Bean
    public ExcelColumnSelectFunction commonStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "common_status"; }
            @Override public List<String> getOptions() { return List.of("正常", "停用"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction booleanSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "boolean_value"; }
            @Override public List<String> getOptions() { return List.of("是", "否"); }
        };
    }

    // ==================== 签约合同 ====================

    @Bean
    public ExcelColumnSelectFunction contractTypeSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "contract_type"; }
            @Override public List<String> getOptions() { return List.of("主合同", "政策合同", "补充协议", "终止协议"); }
        };
    }

    // ==================== 订单 ====================

    @Bean
    public ExcelColumnSelectFunction orderProgressStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_progress_status"; }
            @Override public List<String> getOptions() { return List.of("待确认", "已确认", "已发货", "已签收", "已完成"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction orderPayStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_pay_status"; }
            @Override public List<String> getOptions() { return List.of("未付款", "已付款"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction orderInvoiceStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_invoice_status"; }
            @Override public List<String> getOptions() { return List.of("未开票", "部分开票", "已开票"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction orderPaymentStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_payment_status"; }
            @Override public List<String> getOptions() { return List.of("审批中", "已通过", "已拒绝"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction orderInvoiceRecordStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_invoice_record_status"; }
            @Override public List<String> getOptions() { return List.of("待开票", "已开票"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction orderTimelineNodeCodeSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "order_timeline_node_code"; }
            @Override public List<String> getOptions() { return List.of("下单", "已确认", "已发货", "已签收", "已完成"); }
        };
    }

    // ==================== 售后 ====================

    @Bean
    public ExcelColumnSelectFunction aftersaleHandlingMethodSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "aftersale_handling_method"; }
            @Override public List<String> getOptions() { return List.of("退货", "退换货", "退货退款"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction aftersaleReasonSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "aftersale_reason"; }
            @Override public List<String> getOptions() { return List.of("投诉", "召回", "破损"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction aftersaleProgressStatusSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "aftersale_progress_status"; }
            @Override public List<String> getOptions() { return List.of("待处理", "进行中", "换货中", "已完成"); }
        };
    }

    @Bean
    public ExcelColumnSelectFunction aftersaleNodeCodeSelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "aftersale_node_code"; }
            @Override public List<String> getOptions() { return List.of("申请提交", "审核通过", "商品退回", "退款完成", "红字发票", "新商品发出", "确认收货"); }
        };
    }

    // ==================== 基础数据 ====================

    @Bean
    public ExcelColumnSelectFunction basedataCategorySelect() {
        return new ExcelColumnSelectFunction() {
            @Override public String getName() { return "basedata_category"; }
            @Override public List<String> getOptions() { return List.of("资质文件", "授权文件", "合同文件", "产品文件"); }
        };
    }
}
