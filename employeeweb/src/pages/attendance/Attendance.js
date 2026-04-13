import { useState, useEffect } from "react";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import Table from "react-bootstrap/Table";
import Form from "react-bootstrap/Form";
import Card from "react-bootstrap/Card";
import Badge from "react-bootstrap/Badge";
import "./Attendance.css";

const Attendance = () => {
  const [employees, setEmployees] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [todayAttendance, setTodayAttendance] = useState(null);
  const [monthlyStatistics, setMonthlyStatistics] = useState([]);
  const [employeeAttendanceHistory, setEmployeeAttendanceHistory] = useState([]);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [message, setMessage] = useState({ text: "", type: "" });

  useEffect(() => {
    fetchEmployees();
    fetchMonthlyStatistics();
  }, []);

  useEffect(() => {
    if (selectedEmployee) {
      fetchTodayAttendance(selectedEmployee);
      fetchEmployeeAttendanceHistory(selectedEmployee);
    }
  }, [selectedEmployee]);

  const fetchEmployees = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/employees");
      const data = await response.json();
      setEmployees(data);
    } catch (error) {
      console.error("Error fetching employees:", error.message);
    }
  };

  const fetchTodayAttendance = async (employeeId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/attendance/today/${employeeId}`);
      const data = await response.json();
      setTodayAttendance(data);
    } catch (error) {
      console.error("Error fetching today attendance:", error.message);
    }
  };

  const fetchMonthlyStatistics = async () => {
    try {
      const year = currentMonth.getFullYear();
      const month = currentMonth.getMonth() + 1;
      const response = await fetch(
        `http://localhost:8080/api/attendance/statistics/monthly?year=${year}&month=${month}`
      );
      const data = await response.json();
      setMonthlyStatistics(data);
    } catch (error) {
      console.error("Error fetching monthly statistics:", error.message);
    }
  };

  const fetchEmployeeAttendanceHistory = async (employeeId) => {
    try {
      const year = currentMonth.getFullYear();
      const month = currentMonth.getMonth() + 1;
      const lastDay = new Date(year, month, 0).getDate();
      const startDate = `${year}-${String(month).padStart(2, "0")}-01`;
      const endDate = `${year}-${String(month).padStart(2, "0")}-${lastDay}`;

      const response = await fetch(
        `http://localhost:8080/api/attendance/employee/${employeeId}/range?startDate=${startDate}&endDate=${endDate}`
      );
      const data = await response.json();
      setEmployeeAttendanceHistory(data);
    } catch (error) {
      console.error("Error fetching attendance history:", error.message);
    }
  };

  const handleCheckIn = async () => {
    if (!selectedEmployee) {
      setMessage({ text: "请先选择员工", type: "danger" });
      return;
    }
    try {
      const response = await fetch(
        `http://localhost:8080/api/attendance/checkin/${selectedEmployee}`,
        { method: "POST" }
      );
      if (response.ok) {
        setMessage({ text: "上班打卡成功", type: "success" });
        fetchTodayAttendance(selectedEmployee);
        fetchMonthlyStatistics();
        fetchEmployeeAttendanceHistory(selectedEmployee);
      } else {
        const errorText = await response.text();
        setMessage({ text: errorText, type: "danger" });
      }
    } catch (error) {
      setMessage({ text: "打卡失败: " + error.message, type: "danger" });
    }
    setTimeout(() => setMessage({ text: "", type: "" }), 3000);
  };

  const handleCheckOut = async () => {
    if (!selectedEmployee) {
      setMessage({ text: "请先选择员工", type: "danger" });
      return;
    }
    try {
      const response = await fetch(
        `http://localhost:8080/api/attendance/checkout/${selectedEmployee}`,
        { method: "POST" }
      );
      if (response.ok) {
        setMessage({ text: "下班打卡成功", type: "success" });
        fetchTodayAttendance(selectedEmployee);
        fetchMonthlyStatistics();
        fetchEmployeeAttendanceHistory(selectedEmployee);
      } else {
        const errorText = await response.text();
        setMessage({ text: errorText, type: "danger" });
      }
    } catch (error) {
      setMessage({ text: "打卡失败: " + error.message, type: "danger" });
    }
    setTimeout(() => setMessage({ text: "", type: "" }), 3000);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "NORMAL":
        return <Badge bg="success">正常</Badge>;
      case "LATE":
        return <Badge bg="warning">迟到</Badge>;
      case "EARLY_LEAVE":
        return <Badge bg="danger">早退</Badge>;
      case "ABSENT":
        return <Badge bg="secondary">缺勤</Badge>;
      default:
        return <Badge bg="secondary">未打卡</Badge>;
    }
  };

  const getCalendarData = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = firstDay.getDay();

    const calendarDays = [];

    for (let i = 0; i < startDayOfWeek; i++) {
      calendarDays.push({ day: null, status: null });
    }

    const attendanceMap = {};
    employeeAttendanceHistory.forEach((record) => {
      const date = new Date(record.attendanceDate);
      const day = date.getDate();
      attendanceMap[day] = record.status;
    });

    for (let day = 1; day <= daysInMonth; day++) {
      const dateToCheck = new Date(year, month, day);
      const dayOfWeek = dateToCheck.getDay();
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
      
      calendarDays.push({
        day,
        status: attendanceMap[day] || (isWeekend ? "WEEKEND" : null),
      });
    }

    return calendarDays;
  };

  const getDayStatusClass = (status) => {
    switch (status) {
      case "NORMAL":
        return "day-normal";
      case "LATE":
        return "day-late";
      case "EARLY_LEAVE":
        return "day-early";
      case "ABSENT":
        return "day-absent";
      case "WEEKEND":
        return "day-weekend";
      default:
        return "day-empty";
    }
  };

  const getDayStatusText = (status) => {
    switch (status) {
      case "NORMAL":
        return "正常";
      case "LATE":
        return "迟到";
      case "EARLY_LEAVE":
        return "早退";
      case "ABSENT":
        return "缺勤";
      case "WEEKEND":
        return "周末";
      default:
        return "";
    }
  };

  const changeMonth = (delta) => {
    const newMonth = new Date(currentMonth);
    newMonth.setMonth(newMonth.getMonth() + delta);
    setCurrentMonth(newMonth);
    if (selectedEmployee) {
      fetchEmployeeAttendanceHistory(selectedEmployee);
    }
    fetchMonthlyStatistics();
  };

  const calendarDays = getCalendarData();
  const weekDays = ["日", "一", "二", "三", "四", "五", "六"];

  return (
    <Container className="mt-5">
      {message.text && (
        <div className={`alert alert-${message.type}`} role="alert">
          {message.text}
        </div>
      )}

      <Row className="mb-4">
        <Col>
          <h2 className="text-center">员工考勤管理</h2>
        </Col>
      </Row>

      <Row className="mb-4">
        <Col md={6}>
          <Card>
            <Card.Header>打卡操作</Card.Header>
            <Card.Body>
              <Form.Group className="mb-3">
                <Form.Label>选择员工</Form.Label>
                <Form.Select
                  value={selectedEmployee || ""}
                  onChange={(e) => setSelectedEmployee(e.target.value ? Number(e.target.value) : null)}
                >
                  <option value="">请选择员工</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.name} - {emp.department}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>

              {todayAttendance && selectedEmployee && (
                <Card className="mb-3 bg-light">
                  <Card.Body>
                    <h6>今日打卡状态</h6>
                    <p>
                      上班打卡: {todayAttendance.checkInTime || "未打卡"}
                      {todayAttendance.checkInTime && todayAttendance.status === "LATE" && (
                        <Badge bg="warning" className="ms-2">迟到</Badge>
                      )}
                    </p>
                    <p>
                      下班打卡: {todayAttendance.checkOutTime || "未打卡"}
                      {todayAttendance.checkOutTime && todayAttendance.status === "EARLY_LEAVE" && (
                        <Badge bg="danger" className="ms-2">早退</Badge>
                      )}
                    </p>
                    <p>状态: {getStatusBadge(todayAttendance.status)}</p>
                  </Card.Body>
                </Card>
              )}

              <div className="d-flex gap-2">
                <Button
                  variant="primary"
                  onClick={handleCheckIn}
                  disabled={todayAttendance?.checkInTime !== null}
                >
                  上班打卡
                </Button>
                <Button
                  variant="secondary"
                  onClick={handleCheckOut}
                  disabled={!todayAttendance?.checkInTime || todayAttendance?.checkOutTime !== null}
                >
                  下班打卡
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6}>
          <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
              <span>本月考勤统计</span>
              <div className="d-flex gap-2">
                <Button variant="outline-secondary" size="sm" onClick={() => changeMonth(-1)}>
                  ◀
                </Button>
                <span className="align-self-center">
                  {currentMonth.getFullYear()}年{currentMonth.getMonth() + 1}月
                </span>
                <Button variant="outline-secondary" size="sm" onClick={() => changeMonth(1)}>
                  ▶
                </Button>
              </div>
            </Card.Header>
            <Card.Body>
              <Table striped bordered hover responsive size="sm">
                <thead>
                  <tr>
                    <th>员工</th>
                    <th>出勤天数</th>
                    <th>迟到次数</th>
                    <th>早退次数</th>
                  </tr>
                </thead>
                <tbody>
                  {monthlyStatistics.map((stat) => (
                    <tr key={stat.employeeId}>
                      <td>{stat.employeeName}</td>
                      <td>{stat.attendanceDays}</td>
                      <td>
                        {stat.lateCount > 0 ? (
                          <Badge bg="warning">{stat.lateCount}</Badge>
                        ) : (
                          stat.lateCount
                        )}
                      </td>
                      <td>
                        {stat.earlyLeaveCount > 0 ? (
                          <Badge bg="danger">{stat.earlyLeaveCount}</Badge>
                        ) : (
                          stat.earlyLeaveCount
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row>
        <Col>
          <Card>
            <Card.Header className="d-flex justify-content-between align-items-center">
              <span>考勤日历视图</span>
              {selectedEmployee && (
                <Badge bg="info">
                  {employees.find((e) => e.id === selectedEmployee)?.name || "未选择"}
                </Badge>
              )}
            </Card.Header>
            <Card.Body>
              {!selectedEmployee ? (
                <div className="text-center text-muted py-5">
                  请先选择员工查看考勤日历
                </div>
              ) : (
                <>
                  <div className="calendar-legend mb-3">
                    <span className="legend-item">
                      <span className="legend-box day-normal"></span> 正常
                    </span>
                    <span className="legend-item">
                      <span className="legend-box day-late"></span> 迟到
                    </span>
                    <span className="legend-item">
                      <span className="legend-box day-early"></span> 早退
                    </span>
                    <span className="legend-item">
                      <span className="legend-box day-absent"></span> 缺勤
                    </span>
                    <span className="legend-item">
                      <span className="legend-box day-weekend"></span> 周末
                    </span>
                  </div>

                  <div className="calendar-container">
                    <div className="calendar-header">
                      {weekDays.map((day) => (
                        <div key={day} className="calendar-weekday">
                          {day}
                        </div>
                      ))}
                    </div>
                    <div className="calendar-grid">
                      {calendarDays.map((dayObj, index) => (
                        <div
                          key={index}
                          className={`calendar-day ${dayObj.day ? getDayStatusClass(dayObj.status) : "day-disabled"}`}
                        >
                          {dayObj.day && (
                            <>
                              <div className="day-number">{dayObj.day}</div>
                              {dayObj.status && dayObj.status !== "WEEKEND" && (
                                <div className="day-status">{getDayStatusText(dayObj.status)}</div>
                              )}
                            </>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Attendance;
