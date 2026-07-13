package com.example.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用启动时自动加载 Sentinel 规则。
 *
 * Sentinel 的规则默认存在内存中，服务重启就没了。
 * 这里在每次启动时通过代码加载，保证规则一直生效。
 *
 * Sentinel 控制台（Dashboard）也能在线改规则，
 * 但那些改动也是内存级的，重启就丢。
 * 生产环境应该把规则存在 Nacos 配置中心，实现持久化。
 */
@Component
public class SentinelRuleConfig implements CommandLineRunner {

    @Override
    public void run(String... args) {
        initFlowRules();      // 限流规则
        initDegradeRules();   // 熔断降级规则
        System.out.println(">>> Sentinel 规则已加载");
    }

    /**
     * ===== 限流规则 (Flow Control) =====
     *
     * 作用：限制某个接口在 1 秒内最多处理多少请求。
     *
     * 为什么要限流？
     *   没有任何限制的话，流量突增会打满线程池，
     *   导致所有请求都变慢，最终服务不可用。
     *   限流是在"保护自己"——宁可直接拒绝多余的请求，
     *   也要保证已接受的请求能够正常处理。
     *
     * GRADE_QPS：按每秒请求数限流（另一种是 GRADE_THREAD，按并发线程数）
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule rule = new FlowRule();
        rule.setResource("GET:/api/orders");    // 针对这个 URL 路径
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // 按 QPS 限流
        rule.setCount(10);                      // 每秒最多 10 个请求
        rules.add(rule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * ===== 熔断降级规则 (Circuit Breaking) =====
     *
     * 作用：当调用某个远程服务失败率达到阈值时，"断开电路"，
     * 直接走 fallback 逻辑，给下游服务恢复时间。
     *
     * 为什么要熔断？
     *   类比现实中的空气开关（断路器）：当电流过大时跳闸。
     *   在微服务中，如果 user-service 开始出问题，
     *   继续往它发请求只会让事情更糟（加重负载，堆积连接）。
     *   断开后走降级逻辑 → 过一会儿再试探一下 → 恢复了就闭合。
     *
     * 关键参数：
     *   count=0.5：失败率达到 50% 就熔断
     *   timeWindow=30：熔断 30 秒后尝试半开（放一个请求试探）
     *   minRequestAmount=5：统计窗口内至少有 5 个请求才判断（防止小样本误判）
     *   statIntervalMs=10000：统计窗口 = 10 秒
     *
     * DEGRADE_GRADE_EXCEPTION_RATIO：按异常比例熔断
     * （也可以按慢调用比例 DEGRADE_GRADE_SLOW_REQUEST_RATIO）
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule rule = new DegradeRule();
        // 这个资源名是 Sentinel 自动为 Feign 调用生成的
        // 格式：HTTP方法:http://服务名/路径
        rule.setResource("GET:http://user-service/api/users/{id}");
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setCount(0.5);            // 50% 失败率阈值
        rule.setTimeWindow(30);        // 熔断时长 30 秒
        rule.setMinRequestAmount(5);   // 至少 5 个请求
        rule.setStatIntervalMs(10000); // 统计窗口 10 秒
        rules.add(rule);

        DegradeRuleManager.loadRules(rules);
    }
}
