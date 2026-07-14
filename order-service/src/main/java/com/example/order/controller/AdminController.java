package com.example.order.controller;

import com.example.order.alert.WeComAlertService;
import com.example.order.task.OrderReconciliationTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理接口 —— 手动触发定时任务 + 测试企业微信告警。
 *
 * 这些接口只为了方便测试，生产环境应该通过 JMX 或 Actuator 端点暴露。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderReconciliationTask reconciliationTask;
    private final WeComAlertService weComAlertService;

    public AdminController(OrderReconciliationTask reconciliationTask,
                           WeComAlertService weComAlertService) {
        this.reconciliationTask = reconciliationTask;
        this.weComAlertService = weComAlertService;
    }

    /**
     * 手动触发一次对账任务。
     * GET /api/admin/reconcile
     */
    @GetMapping("/reconcile")
    public Map<String, Object> triggerReconciliation() {
        try {
            String result = reconciliationTask.doReconciliation();
            return Map.of("success", true, "result", result);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 发送一条测试告警到企业微信，验证 Webhook 配置是否正确。
     * GET /api/admin/test-alert
     */
    @GetMapping("/test-alert")
    public Map<String, Object> testAlert() {
        weComAlertService.sendAlert(
                "【测试】企业微信告警通道验证",
                "如果你看到这条消息，说明 Webhook 配置正确。\n定时任务告警将使用此通道发送。"
        );
        return Map.of("success", true, "message", "测试告警已发送，请检查企业微信群");
    }
}
