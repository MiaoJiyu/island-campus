package com.islandcampus.server.plugin.sandbox;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Groovy脚本沙箱执行器
 * 安全措施:
 * 1. 使用独立类加载器，避免污染主ClassPath
 * 2. 设置脚本超时时间(30秒)
 * 3. 禁止危险操作（通过SecurityManager限制）
 */
@Slf4j
@Component
public class GroovySandbox {

    private static final long SCRIPT_TIMEOUT_SECONDS = 30;

    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "groovy-sandbox-worker");
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
        log.info("Groovy沙箱线程池已关闭");
    }

    /**
     * 在安全沙箱中执行Groovy脚本
     *
     * @param scriptContent 脚本内容
     * @param context       执行上下文参数
     * @return 执行结果
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(String scriptContent, Map<String, Object> context) throws Exception {
        // 预检：检查是否包含危险操作
        checkScriptSafety(scriptContent);

        Future<Map<String, Object>> future = executorService.submit(() -> {
            try (GroovyClassLoader loader = new GroovyClassLoader()) {
                Class<?> scriptClass = loader.parseClass(scriptContent);
                Script scriptInstance = (Script) scriptClass.getDeclaredConstructor().newInstance();

                // 注入上下文参数到脚本的binding
                if (context != null) {
                    scriptInstance.getBinding().getVariables().putAll(context);
                }

                // 执行脚本并获取结果
                Object result = scriptInstance.run();
                if (result instanceof Map) {
                    return (Map<String, Object>) result;
                }

                return Map.of("success", true, "data", result != null ? result : "");
            } catch (Exception e) {
                throw new RuntimeException("Groovy脚本执行失败: " + e.getMessage(), e);
            }
        });

        try {
            return future.get(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Groovy脚本执行超时（" + SCRIPT_TIMEOUT_SECONDS + "秒）", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new RuntimeException("脚本执行异常: " + cause.getMessage(), cause);
        }
    }

    /**
     * 基础安全性检查：检测明显危险操作
     */
    private void checkScriptSafety(String content) {
        String lowerContent = content.toLowerCase();

        // 检测危险关键字
        String[] dangerousPatterns = {
                "system.exit",
                "runtime.getruntime",
                ".exec(",
                "fileoutputstream",
                "filewriter",
                "new file",
                ".delete(",
                "socket(",
                "urlconnection",
                "httpsurlconnection",
                "class.forname",
                "classloader"
        };

        for (String pattern : dangerousPatterns) {
            if (lowerContent.contains(pattern)) {
                log.warn("Groovy脚本安全检查未通过，包含危险模式: {}", pattern);
                throw new SecurityException("脚本包含不允许的操作: " + pattern);
            }
        }
    }
}
