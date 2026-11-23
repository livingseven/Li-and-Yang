package com.example.taskapi.service;

import com.example.taskapi.common.BusinessException;
import com.example.taskapi.dto.TaskRequest;
import com.example.taskapi.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// 业务逻辑层：只负责"业务上发生了什么"，不关心 HTTP 和 JSON。
// 查不到数据时直接抛 BusinessException，由全局异常处理器统一翻译成 404。
@Service
public class TaskService {

    // 用线程安全的 Map 当"数据库"，AtomicLong 生成自增 id；重启即清空
    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Task> list() {
        return new ArrayList<>(store.values());
    }

    public Task getOrThrow(Long id) {
        Task task = store.get(id);
        if (task == null) {
            throw BusinessException.notFound("任务 " + id);
        }
        return task;
    }

    public Task create(String title, String description) {
        Long id = idCounter.getAndIncrement();
        Task task = new Task(id, title, description, false);
        store.put(id, task);
        return task;
    }

    // 局部更新：只改请求里非 null 的字段
    public Task update(Long id, TaskRequest req) {
        Task existing = getOrThrow(id);
        if (req.getTitle() != null) {
            existing.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            existing.setDescription(req.getDescription());
        }
        if (req.getCompleted() != null) {
            existing.setCompleted(req.getCompleted());
        }
        return existing;
    }

    public void delete(Long id) {
        if (store.remove(id) == null) {
            throw BusinessException.notFound("任务 " + id);
        }
    }

    // 供自动化测试在每个用例前重置内存数据，保证用例互不干扰
    public void clear() {
        store.clear();
        idCounter.set(1);
    }
}
