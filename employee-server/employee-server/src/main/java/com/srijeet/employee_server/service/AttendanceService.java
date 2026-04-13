package com.srijeet.employee_server.service;

import com.srijeet.employee_server.dto.AttendanceStatisticsDTO;
import com.srijeet.employee_server.dto.DailyAttendanceDTO;
import com.srijeet.employee_server.entity.Attendance;
import com.srijeet.employee_server.entity.Employee;
import com.srijeet.employee_server.repository.AttendanceRepository;
import com.srijeet.employee_server.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Attendance checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getCheckInTime() != null) {
                throw new IllegalStateException("Already checked in today");
            }
            attendance.setCheckInTime(LocalTime.now());
            updateAttendanceStatus(attendance);
            return attendanceRepository.save(attendance);
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(LocalTime.now());
        updateAttendanceStatus(attendance);

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new IllegalStateException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out today");
        }

        attendance.setCheckOutTime(LocalTime.now());
        updateAttendanceStatus(attendance);

        return attendanceRepository.save(attendance);
    }

    private void updateAttendanceStatus(Attendance attendance) {
        LocalTime checkInTime = attendance.getCheckInTime();
        LocalTime checkOutTime = attendance.getCheckOutTime();

        if (checkInTime == null) {
            attendance.setStatus(Attendance.AttendanceStatus.ABSENT);
            return;
        }

        boolean isLate = checkInTime.isAfter(LocalTime.of(9, 0));
        boolean isEarlyLeave = checkOutTime != null && checkOutTime.isBefore(LocalTime.of(18, 0));

        if (isLate && isEarlyLeave) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        } else if (isLate) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        } else if (isEarlyLeave) {
            attendance.setStatus(Attendance.AttendanceStatus.EARLY_LEAVE);
        } else if (checkOutTime != null) {
            attendance.setStatus(Attendance.AttendanceStatus.NORMAL);
        } else {
            attendance.setStatus(Attendance.AttendanceStatus.NORMAL);
        }
    }

    public List<DailyAttendanceDTO> getTodayAttendance() {
        LocalDate today = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateOrderByEmployeeId(today);
        return convertToDTOList(attendances);
    }

    public List<DailyAttendanceDTO> getEmployeeAttendanceHistory(Long employeeId) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId);
        return convertToDTOList(attendances);
    }

    public List<DailyAttendanceDTO> getEmployeeAttendanceByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        return convertToDTOList(attendances);
    }

    public List<AttendanceStatisticsDTO> getMonthlyStatistics(int year, int month) {
        LocalTime lateTime = LocalTime.of(9, 0);
        LocalTime earlyTime = LocalTime.of(18, 0);
        List<Object[]> results = attendanceRepository.getMonthlyStatistics(year, month, lateTime, earlyTime);
        return convertStatisticsToDTO(results);
    }

    public AttendanceStatisticsDTO getMonthlyStatisticsByEmployee(Long employeeId, int year, int month) {
        LocalTime lateTime = LocalTime.of(9, 0);
        LocalTime earlyTime = LocalTime.of(18, 0);
        List<Object[]> results = attendanceRepository.getMonthlyStatisticsByEmployee(employeeId, year, month, lateTime, earlyTime);
        if (results.isEmpty()) {
            return null;
        }
        return convertStatisticsToDTO(results).get(0);
    }

    private List<DailyAttendanceDTO> convertToDTOList(List<Attendance> attendances) {
        List<DailyAttendanceDTO> dtos = new ArrayList<>();
        for (Attendance attendance : attendances) {
            DailyAttendanceDTO dto = new DailyAttendanceDTO();
            dto.setId(attendance.getId());
            dto.setEmployeeId(attendance.getEmployee().getId());
            dto.setEmployeeName(attendance.getEmployee().getName());
            dto.setAttendanceDate(attendance.getAttendanceDate());
            dto.setCheckInTime(attendance.getCheckInTime());
            dto.setCheckOutTime(attendance.getCheckOutTime());
            dto.setStatus(attendance.getStatus() != null ? attendance.getStatus().name() : null);
            dtos.add(dto);
        }
        return dtos;
    }

    private List<AttendanceStatisticsDTO> convertStatisticsToDTO(List<Object[]> results) {
        List<AttendanceStatisticsDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
            dto.setEmployeeId(((Number) row[0]).longValue());
            dto.setEmployeeName((String) row[1]);
            dto.setAttendanceDays(((Number) row[2]).intValue());
            dto.setLateCount(((Number) row[3]).intValue());
            dto.setEarlyLeaveCount(((Number) row[4]).intValue());
            dtos.add(dto);
        }
        return dtos;
    }

    public Optional<Attendance> getTodayAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, LocalDate.now());
    }
}
