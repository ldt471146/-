package com.example.back.controller;

import com.example.back.exception.GlobalExceptionHandler;
import com.example.back.service.ExamService;
import com.example.back.service.ExamTaskService;
import com.example.back.vo.ExamCreateVO;
import com.example.back.vo.ExamSubmissionVO;
import com.example.back.vo.ExamTaskVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamTaskControllerWebTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ExamTaskController.class, GlobalExceptionHandler.class})
class ExamTaskControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamTaskService examTaskService;

    @MockBean
    private ExamService examService;

    @Test
    void shouldListStudentTasks() throws Exception {
        ExamTaskVO task = new ExamTaskVO();
        task.setId(1L);
        task.setTitle("循环专项周测");
        task.setStatus(1);
        when(examTaskService.listStudentTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/exam-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("循环专项周测"));
    }

    @Test
    void shouldListMySubmissions() throws Exception {
        ExamSubmissionVO submission = new ExamSubmissionVO();
        submission.setId(11L);
        submission.setTaskId(1L);
        submission.setScore(88);
        when(examTaskService.listMySubmissions()).thenReturn(List.of(submission));

        mockMvc.perform(get("/api/exam-tasks/my-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].score").value(88));
    }

    @Test
    void shouldStartTaskExam() throws Exception {
        ExamCreateVO createVO = new ExamCreateVO();
        createVO.setExamId("task_exam_1");
        createVO.setDurationMinutes(30);
        when(examService.createTaskExam(1L)).thenReturn(createVO);

        mockMvc.perform(post("/api/exam-tasks/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.examId").value("task_exam_1"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
