package com.example.taskapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

// Task 是领域模型/实体，也直接作为接口返回的数据（放在 ApiResponse.data 里）
@Schema(description = "任务实体")
public class Task {

    @Schema(description = "任务编号，由服务端生成", example = "1")
    private Long id;

    @Schema(description = "任务标题", example = "写周报")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "是否已完成", example = "false")
    private boolean completed;

    public Task() {
        // 无参构造器：Jackson 序列化/反序列化需要
    }

    public Task(Long id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
