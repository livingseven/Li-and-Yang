package com.example.taskapi.controller;

import com.example.taskapi.common.ApiResponse;
import com.example.taskapi.dto.TaskRequest;
import com.example.taskapi.model.Task;
import com.example.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller 只做三件事：接请求、触发校验、把结果包进 ApiResponse。
// 业务判断在 Service，异常翻译成响应在 GlobalExceptionHandler，这里看不到 try-catch。
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理", description = "Task 的增删改查")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "新增任务（title 必填）")
    public ResponseEntity<ApiResponse<Task>> create(
            @Validated(TaskRequest.Create.class) @RequestBody TaskRequest req) {
        Task task = taskService.create(req.getTitle(), req.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Task>success(task));
    }

    @GetMapping
    @Operation(summary = "任务列表")
    public ApiResponse<List<Task>> list() {
        return ApiResponse.success(taskService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "任务详情；不存在返回 404")
    public ApiResponse<Task> detail(@PathVariable Long id) {
        return ApiResponse.success(taskService.getOrThrow(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "局部修改任务；不存在返回 404")
    public ApiResponse<Task> update(
            @PathVariable Long id,
            @Validated(TaskRequest.Update.class) @RequestBody TaskRequest req) {
        return ApiResponse.success(taskService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务；不存在返回 404")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.<Void>success(null);
    }
}
