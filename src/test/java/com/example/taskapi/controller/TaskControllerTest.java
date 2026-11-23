package com.example.taskapi.controller;

import com.example.taskapi.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// MockMvc 不启动真实 Tomcat、不发真实 HTTP 请求，直接把请求喂给 Spring MVC，
// 所以能又快又稳地测「正常请求 / 参数错误 / 数据不存在」三类情况。
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetStore() {
        taskService.clear();
    }

    private String body(String title, String description, Boolean completed) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (title != null) {
            map.put("title", title);
        }
        if (description != null) {
            map.put("description", description);
        }
        if (completed != null) {
            map.put("completed", completed);
        }
        return objectMapper.writeValueAsString(map);
    }

    @Test
    @DisplayName("正常请求：健康检查返回统一结构")
    void healthReturnsWrappedResponse() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("task-api"));
    }

    @Test
    @DisplayName("正常请求：新增任务返回 201 和包装后的数据")
    void createReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("写周报", "周五前完成", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("写周报"))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("参数错误：标题为空返回 400 + 校验错误码 40002")
    void createWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "周五前完成", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40002))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("参数错误：标题超过 100 字返回 400 + 校验错误码 40002")
    void createWithTooLongTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("A".repeat(101), null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40002));
    }

    @Test
    @DisplayName("正常请求：列表返回统一结构")
    void listReturnsWrappedArray() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("写周报", "周五前完成", null)));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("写周报"));
    }

    @Test
    @DisplayName("数据不存在：查询不存在的任务返回 404 + 40401")
    void getMissingReturns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("数据不存在：修改不存在的任务返回 404 + 40401")
    void updateMissingReturns404() throws Exception {
        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(null, null, true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("数据不存在：删除不存在的任务返回 404 + 40401")
    void deleteMissingReturns404() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("分组校验：修改时只传 completed 也能通过")
    void updateWithCompletedOnlyPasses() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("写周报", null, null)));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(null, null, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.title").value("写周报"));
    }

    @Test
    @DisplayName("正常请求：增删改查全流程都返回统一结构")
    void fullCrudWorksEndToEnd() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("写周报", "周五前完成", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("写周报"));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("写周报（改）", null, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("写周报（改）"));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }
}
