package com.nabin.workflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatsDTO {
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long doneTasks;
    private long overdueTasks;
}