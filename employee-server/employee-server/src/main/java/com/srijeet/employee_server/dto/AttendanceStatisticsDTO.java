package com.srijeet.employee_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStatisticsDTO {
    private Long employeeId;
    private String employeeName;
    private int attendanceDays;
    private int lateCount;
    private int earlyLeaveCount;
}
