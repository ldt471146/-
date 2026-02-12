package com.example.back.controller;

import com.example.back.service.ExamService;
import com.example.back.vo.ExamSubmitVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExamController.class)
@AutoConfigureMockMvc(addFilters = false)
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
}

