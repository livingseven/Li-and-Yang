package com.example.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// TaskRequest 是请求体 DTO，和返回的 Task 分开：客户端不该传 id，也不强制每次都传 completed
@Schema(description = "新增 / 修改任务的请求体")
public class TaskRequest {

    // 校验分组：新增时 title 必填；修改时允许不传 title（只改 completed 也可以）
    public interface Create {
    }

    public interface Update {
    }

    @NotBlank(message = "标题不能为空", groups = Create.class)
    @Size(max = 100, message = "标题最多 100 个字符", groups = {Create.class, Update.class})
    @Schema(description = "任务标题，新增时必填；修改时可不传", example = "写周报")
    private String title;

    @Size(max = 500, message = "描述最多 500 个字符", groups = {Create.class, Update.class})
    @Schema(description = "任务描述，可为空", example = "周五前完成")
    private String description;

    @Schema(description = "是否完成；不传表示不修改该字段", example = "true")
    private Boolean completed;

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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
