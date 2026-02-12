package com.example.back.controller;

import com.example.back.service.CodeProblemService;
import com.example.back.vo.CodeSubmitResultVO;
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

@WebMvcTest(CodeProblemController.class)
@AutoConfigureMockMvc(addFilters = false)
class CodeProblemControllerSubmitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeProblemService codeProblemService;

    @Test
    void shouldReturnUnifiedJudgeResultFields() throws Exception {
        CodeSubmitResultVO vo = new CodeSubmitResultVO();
        vo.setProblemId(1L);
        vo.setResult("CE");
        vo.setResultLabel("Compile Error");
        vo.setErrorType("COMPILE_ERROR");
        vo.setFailedCaseIndex(1);
        vo.setPassed(0);
        vo.setTotal(2);
        vo.setMessages(List.of("编译失败：missing ';'"));
        when(codeProblemService.submit(any())).thenReturn(vo);

        mockMvc.perform(post("/api/code/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemId": 1,
                                  "languageId": 54,
                                  "sourceCode": "int main(){ return 0 }"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.result").value("CE"))
                .andExpect(jsonPath("$.data.resultLabel").value("Compile Error"))
                .andExpect(jsonPath("$.data.errorType").value("COMPILE_ERROR"))
                .andExpect(jsonPath("$.data.failedCaseIndex").value(1));
    }

    @Test
    void shouldReturnBusinessErrorWhenSubmitFails() throws Exception {
        when(codeProblemService.submit(any())).thenThrow(new IllegalArgumentException("题目不存在"));

        mockMvc.perform(post("/api/code/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemId": 99999,
                                  "languageId": 54,
                                  "sourceCode": "int main(){ return 0; }"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("题目不存在"));
    }
}
