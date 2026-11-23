package com.example.taskapi.common;

// 错误码表：业务码(code) 与 HTTP 状态码分开。
// HTTP 状态码给网关/浏览器看，业务码给前端和日志看，可以分得更细。
public enum ErrorCode {

    SUCCESS(0, "success", 200),
    BAD_REQUEST(40001, "请求格式不正确", 400),
    VALIDATION_FAILED(40002, "参数校验未通过", 400),
    NOT_FOUND(40401, "数据不存在", 404),
    INTERNAL_ERROR(50000, "服务器内部错误", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
