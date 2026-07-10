package com.taiyidu.taiyidu.gongsheng.result;
import lombok.Data;

@Data
public class Result<T> {

    private Integer code;    // 状态码：200代表成功，400/500/403等代表失败
    private String message;  // 提示信息：给前端弹窗展示给用户看的（如 "解析成功"、"服务器冒烟了"）
    private T data;          // 核心业务数据：可以是具体的视频对象、或者是图片数组

    /**
     * 成功返回：带数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回：不带数据（仅提示）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败返回：自定义状态码与错误信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 失败返回：默认 500 通用错误
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
