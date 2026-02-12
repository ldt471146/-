package com.example.back.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.controller.AdminUserController;
import com.example.back.controller.CodeProblemController;
import com.example.back.controller.TeacherController;
import com.example.back.dto.CodeSubmitRequest;
import com.example.back.entity.SysUser;
import com.example.back.mapper.SysRoleMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.mapper.SysUserRoleMapper;
import com.example.back.security.JwtAuthFilter;
import com.example.back.security.UserDetailsServiceImpl;
import com.example.back.service.CodeProblemService;
import com.example.back.service.TeacherService;
import com.example.back.util.JwtUtil;
import com.example.back.vo.CodeSubmitResultVO;
import com.example.back.vo.TeacherStatsOverviewVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T002: 关键流程集成测试骨架（认证、角色权限、关键接口连通性）。
 */
@WebMvcTest({
        CodeProblemController.class,
        TeacherController.class,
        AdminUserController.class
})
@AutoConfigureMockMvc
@Import({com.example.back.config.SecurityConfig.class, JwtAuthFilter.class})
class ApiSecurityFlowSkeletonTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeProblemService codeProblemService;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private SysUserMapper userMapper;

    @MockBean
    private SysRoleMapper roleMapper;

    @MockBean
    private SysUserRoleMapper userRoleMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("未登录访问受保护接口应返回 401")
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/code/problems"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("学生可提交编程题并拿到统一结果结构")
    void shouldAllowStudentSubmitCode() throws Exception {
        CodeSubmitResultVO result = new CodeSubmitResultVO();
        result.setProblemId(1L);
        result.setResult("AC");
        result.setResultLabel("Accepted");
        result.setErrorType("NONE");
        result.setPassed(2);
        result.setTotal(2);
        when(codeProblemService.submit(any(CodeSubmitRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/code/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemId": 1,
                                  "languageId": 71,
                                  "sourceCode": "print(1)"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.result").value("AC"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("学生访问管理员用户管理接口应被拒绝")
    void shouldForbidStudentAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("管理员可访问用户管理列表")
    void shouldAllowAdminAccessAdminApi() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student-a");
        user.setEmail("student-a@example.com");
        user.setStatus(1);

        Page<SysUser> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(user));
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(roleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));

        mockMvc.perform(get("/api/admin/users?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].username").value("student-a"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("学生访问教师统计接口应被拒绝")
    void shouldForbidStudentAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    @DisplayName("教师可访问统计接口")
    void shouldAllowTeacherAccessTeacherApi() throws Exception {
        TeacherStatsOverviewVO overview = new TeacherStatsOverviewVO();
        overview.setTotalCourses(3);
        overview.setTotalStudents(42);
        overview.setTotalSubmissions(128);
        overview.setAvgScore(86.5);
        when(teacherService.statsOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/teacher/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalCourses").value(3));
    }
}
