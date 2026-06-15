package cn.iocoder.yudao.module.opshub.framework.datapermission.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerUserScopeMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.ExecutorProductLineScopeMapper;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于经销商和产品线的 {@link DataPermissionRule} 数据权限规则实现
 * <p>
 * 核心逻辑：
 * 1. super_admin → 返回 null（不过滤，查看全部数据）
 * 2. brand_admin / brand_sales / service_executor → 基于产品线维度过滤（product_line_code IN ...）
 * 3. dealer → 基于经销商维度过滤（dealer_code IN ...）
 * 4. 其他角色 → 返回 null（不过滤）
 * <p>
 * 与 DeptDataPermissionRule 共存，通过 @DataPermission(includeRules = ...) 注解按方法控制启用哪个规则。
 */
@Slf4j
public class DealerDataPermissionRule implements DataPermissionRule {

    /**
     * LoginUser 的 Context 缓存 Key
     */
    private static final String CONTEXT_KEY = DealerDataPermissionRule.class.getSimpleName();

    private static final String DEALER_COLUMN_NAME = "dealer_code";
    private static final String PRODUCT_LINE_COLUMN_NAME = "product_line_code";

    private final PermissionCommonApi permissionApi;
    private final DealerUserScopeMapper dealerUserScopeMapper;
    private final ExecutorProductLineScopeMapper executorProductLineScopeMapper;

    /**
     * 基于经销商 Code 的表字段配置
     * key：表名，value：字段名
     */
    private final Map<String, String> dealerColumns = new HashMap<>();

    /**
     * 基于产品线 Code 的表字段配置
     * key：表名，value：字段名
     */
    private final Map<String, String> productLineColumns = new HashMap<>();

    /**
     * 所有表名集合
     */
    private final Set<String> TABLE_NAMES = new HashSet<>();

    public DealerDataPermissionRule(PermissionCommonApi permissionApi,
                                    DealerUserScopeMapper dealerUserScopeMapper,
                                    ExecutorProductLineScopeMapper executorProductLineScopeMapper) {
        this.permissionApi = permissionApi;
        this.dealerUserScopeMapper = dealerUserScopeMapper;
        this.executorProductLineScopeMapper = executorProductLineScopeMapper;
    }

    @Override
    public Set<String> getTableNames() {
        return TABLE_NAMES;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        // 只有有登陆用户的情况下，才进行数据权限的处理
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        // 只有管理员类型的用户，才进行数据权限的处理
        if (ObjectUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            return null;
        }

        // 从上下文中获取缓存数据
        DealerPermissionData permissionData = loginUser.getContext(CONTEXT_KEY, DealerPermissionData.class);
        if (permissionData == null) {
            permissionData = buildPermissionData(loginUser.getId());
            loginUser.setContext(CONTEXT_KEY, permissionData);
        }

        // 情况一：ALL — 可查看全部数据，无需拼接条件
        if (permissionData.getAll()) {
            return null;
        }

        // 情况二：经销商维度
        if (permissionData.getDealerScope()) {
            return buildDealerExpression(tableName, tableAlias, permissionData.getDealerCodes());
        }

        // 情况三：产品线维度
        if (permissionData.getProductLineScope()) {
            return buildProductLineExpression(tableName, tableAlias, permissionData.getProductLineCodes());
        }

        // 兜底：无匹配角色，不过滤
        return null;
    }

    /**
     * 构建当前用户的权限数据（首次计算时调用，结果缓存到 LoginUser.context）
     */
    private DealerPermissionData buildPermissionData(Long userId) {
        DealerPermissionData data = new DealerPermissionData();

        // 1. super_admin → 全部可见
        if (permissionApi.hasAnyRoles(userId, "super_admin")) {
            data.setAll(true);
            return data;
        }

        // 2. 产品线维度角色：brand_admin / brand_sales / service_executor
        if (permissionApi.hasAnyRoles(userId,
                OpsRoleCodeConstants.BRAND_ADMIN,
                OpsRoleCodeConstants.BRAND_SALES,
                OpsRoleCodeConstants.SERVICE_EXECUTOR)) {
            data.setProductLineScope(true);
            Set<String> productLineCodes = executorProductLineScopeMapper.selectProductLineCodesByUserId(userId);
            data.setProductLineCodes(productLineCodes);
        }

        // 3. 经销商维度角色：dealer
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.DEALER)) {
            data.setDealerScope(true);
            Set<String> dealerCodes = dealerUserScopeMapper.selectDealerCodesByUserId(userId);
            data.setDealerCodes(dealerCodes);
        }

        return data;
    }

    /**
     * 构建经销商维度 WHERE 条件：WHERE dealer_code IN ('HK', 'ZS')
     */
    private Expression buildDealerExpression(String tableName, Alias tableAlias, Set<String> dealerCodes) {
        String columnName = dealerColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 经销商 Code 为空 → 无权查看任何数据
        if (CollUtil.isEmpty(dealerCodes)) {
            return new EqualsTo(null, null);
        }
        return new InExpression(
                MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                new ParenthesedExpressionList(new ExpressionList<StringValue>(
                        CollectionUtils.convertList(dealerCodes, StringValue::new))));
    }

    /**
     * 构建产品线维度 WHERE 条件：WHERE product_line_code IN ('GK', 'FK')
     */
    private Expression buildProductLineExpression(String tableName, Alias tableAlias, Set<String> productLineCodes) {
        String columnName = productLineColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 产品线 Code 为空 → 无权查看任何数据
        if (CollUtil.isEmpty(productLineCodes)) {
            return new EqualsTo(null, null);
        }
        return new InExpression(
                MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                new ParenthesedExpressionList(new ExpressionList<StringValue>(
                        CollectionUtils.convertList(productLineCodes, StringValue::new))));
    }

    // ==================== 添加配置 ====================

    /**
     * 添加经销商 Code 列的过滤配置
     *
     * @param tableName  表名
     * @param columnName 列名（默认 dealer_code）
     */
    public void addDealerColumn(String tableName, String columnName) {
        dealerColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }

    public void addDealerColumn(String tableName) {
        addDealerColumn(tableName, DEALER_COLUMN_NAME);
    }

    /**
     * 添加产品线 Code 列的过滤配置
     *
     * @param tableName  表名
     * @param columnName 列名（默认 product_line_code）
     */
    public void addProductLineColumn(String tableName, String columnName) {
        productLineColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }

    public void addProductLineColumn(String tableName) {
        addProductLineColumn(tableName, PRODUCT_LINE_COLUMN_NAME);
    }

    // ==================== 内部缓存 DTO ====================

    /**
     * 经销商权限数据（缓存在 LoginUser.context 中，请求级有效）
     */
    @Data
    static class DealerPermissionData {
        /** 是否可查看全部数据（super_admin） */
        private Boolean all = false;
        /** 是否为经销商维度角色 */
        private Boolean dealerScope = false;
        /** 是否为产品线维度角色 */
        private Boolean productLineScope = false;
        /** 授权的经销商 Code 集合 */
        private Set<String> dealerCodes;
        /** 授权的产品线 Code 集合 */
        private Set<String> productLineCodes;
    }

}
