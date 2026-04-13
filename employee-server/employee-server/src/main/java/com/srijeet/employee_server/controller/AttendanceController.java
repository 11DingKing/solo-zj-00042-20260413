package com.srijeet.employee_server.controller;

import com.srijeet.employee_server.dto.AttendanceStatisticsDTO;
import com.srijeet.employee_server.dto.DailyAttendanceDTO;
import com.srijeet.employee_server.entity.Attendance;
import com.srijeet.employee_server.service.AttendanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/attendance/checkin/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.checkIn(employeeId);
            return ResponseEntity.ok(convertToDTO(attendance));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/attendance/checkout/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.checkOut(employeeId);
            return ResponseEntity.ok(convertToDTO(attendance));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/attendance/today")
    public ResponseEntity<List<DailyAttendanceDTO>> getTodayAttendance() {
        List<DailyAttendanceDTO> attendances = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/attendance/today/{employeeId}")
    public ResponseEntity<?> getTodayAttendanceByEmployee(@PathVariable Long employeeId) {
        Optional<Attendance> attendance = attendanceService.getTodayAttendanceByEmployee(employeeId);
        if (attendance.isPresent()) {
            return ResponseEntity.ok(convertToDTO(attendance.get()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("attendanceDate", LocalDate.now());
        result.put("checkInTime", null);
        result.put("checkOutTime", null);
        result.put("status", "NOT_CHECKED_IN");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attendance/employee/{employeeId}")
    public ResponseEntity<List<DailyAttendanceDTO>> getEmployeeAttendanceHistory(@PathVariable Long employeeId) {
        List<DailyAttendanceDTO> attendances = attendanceService.getEmployeeAttendanceHistory(employeeId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/attendance/employee/{employeeId}/range")
    public ResponseEntity<List<DailyAttendanceDTO>> getEmployeeAttendanceByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyAttendanceDTO> attendances = attendanceService.getEmployeeAttendanceByDateRange(employeeId, startDate, endDate);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/attendance/statistics/monthly")
    public ResponseEntity<List<AttendanceStatisticsDTO>> getMonthlyStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        List<AttendanceStatisticsDTO> statistics = attendanceService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/attendance/statistics/monthly/{employeeId}")
    public ResponseEntity<?> getMonthlyStatisticsByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        AttendanceStatisticsDTO statistics = attendanceService.getMonthlyStatisticsByEmployee(employeeId, year, month);
        if (statistics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statistics);
    }

    private DailyAttendanceDTO convertToDTO(Attendance attendance) {
        DailyAttendanceDTO dto = new DailyAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEmployeeId(attendance.getEmployee().getId());
        dto.setEmployeeName(attendance.getEmployee().getName());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setStatus(attendance.getStatus() != null ? attendance.getStatus().name() : null);
        return dto;
    }
}
