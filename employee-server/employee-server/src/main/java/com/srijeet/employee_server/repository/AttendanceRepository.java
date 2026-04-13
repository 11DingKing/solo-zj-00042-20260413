package com.srijeet.employee_server.repository;

import com.srijeet.employee_server.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);

    List<Attendance> findByAttendanceDateOrderByEmployeeId(LocalDate attendanceDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate")
    List<Attendance> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT 
                e.id as employeeId,
                e.name as employeeName,
                COUNT(CASE WHEN a.checkInTime IS NOT NULL THEN 1 END) as attendanceDays,
                COUNT(CASE WHEN a.checkInTime IS NOT NULL AND a.checkInTime > :lateTime THEN 1 END) as lateCount,
                COUNT(CASE WHEN a.checkOutTime IS NOT NULL AND a.checkOutTime < :earlyTime THEN 1 END) as earlyLeaveCount
            FROM Employee e
            LEFT JOIN Attendance a ON e.id = a.employee.id 
                AND FUNCTION('MONTH', a.attendanceDate) = :month 
                AND FUNCTION('YEAR', a.attendanceDate) = :year
            GROUP BY e.id, e.name
            ORDER BY e.id
            """)
    List<Object[]> getMonthlyStatistics(
            @Param("year") int year,
            @Param("month") int month,
            @Param("lateTime") LocalTime lateTime,
            @Param("earlyTime") LocalTime earlyTime
    );

    @Query("""
            SELECT 
                e.id as employeeId,
                e.name as employeeName,
                COUNT(CASE WHEN a.checkInTime IS NOT NULL THEN 1 END) as attendanceDays,
                COUNT(CASE WHEN a.checkInTime IS NOT NULL AND a.checkInTime > :lateTime THEN 1 END) as lateCount,
                COUNT(CASE WHEN a.checkOutTime IS NOT NULL AND a.checkOutTime < :earlyTime THEN 1 END) as earlyLeaveCount
            FROM Employee e
            LEFT JOIN Attendance a ON e.id = a.employee.id 
                AND FUNCTION('MONTH', a.attendanceDate) = :month 
                AND FUNCTION('YEAR', a.attendanceDate) = :year
            WHERE e.id = :employeeId
            GROUP BY e.id, e.name
            """)
    List<Object[]> getMonthlyStatisticsByEmployee(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("lateTime") LocalTime lateTime,
            @Param("earlyTime") LocalTime earlyTime
    );
}
