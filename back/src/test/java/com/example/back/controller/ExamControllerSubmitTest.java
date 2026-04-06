package com.example.back.controller;

import com.example.back.exception.GlobalExceptionHandler;
import com.example.back.service.ExamService;
import com.example.back.vo.ExamSubmitVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamControllerSubmitTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ExamController.class, GlobalExceptionHandler.class})
class ExamControllerSubmitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    @Test
    void shouldReturnScoreForSubmit() throws Exception {
        ExamSubmitVO vo = new ExamSubmitVO();
        vo.setTotal(10);
        vo.setCorrectCount(8);
        vo.setWrongCount(2);
        vo.setScore(80);
        vo.setResults(List.of());
        when(examService.submitMockExam(any())).thenReturn(vo);

        mockMvc.perform(post("/api/exams/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examId": "exam_001",
                                  "answers": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score").value(80))
                .andExpect(jsonPath("$.data.correctCount").value(8));
    }

    @Test
    void shouldReturnValidationErrorWhenExamIdMissing() throws Exception {
        mockMvc.perform(post("/api/exams/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examId": "",
                                  "answers": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("考试ID不能为空"));

        verifyNoInteractions(examService);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
