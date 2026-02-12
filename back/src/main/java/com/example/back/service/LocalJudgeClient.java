package com.example.back.service;

import com.example.back.config.LocalJudgeProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 本地判题执行器（轻量版）
 */
@Component
public class LocalJudgeClient {

    private final LocalJudgeProperties properties;

    public LocalJudgeClient(LocalJudgeProperties properties) {
        this.properties = properties;
    }

    public JudgeOutcome judgeAll(String sourceCode,
                                 Integer languageId,
                                 List<TestCaseInput> testCases) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return JudgeOutcome.fail("CE", List.of("代码不能为空"), 0, testCases.size());
        }
        if (testCases == null || testCases.isEmpty()) {
            return JudgeOutcome.fail("WA", List.of("题目未配置测试用例"), 0, 0);
        }

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("local-judge-");
            ProgramRunner runner = buildRunner(languageId, sourceCode, workDir);
            CompileResult compileResult = runner.compile();
            if (!compileResult.isSuccess()) {
                return JudgeOutcome.fail("CE", List.of(safeMsg("编译失败：" + compileResult.getMessage())), 0, testCases.size());
            }

            int passed = 0;
            List<String> messages = new ArrayList<>();
            for (int i = 0; i < testCases.size(); i++) {
                TestCaseInput testCase = testCases.get(i);
                RunResult runResult = runner.run(testCase.getInput());
                int index = i + 1;
                if (runResult.isTimeout()) {
                    messages.add("第" + index + "组：超时");
                    return new JudgeOutcome("TLE", passed, testCases.size(), messages);
                }
                if (!runResult.isSuccess()) {
                    messages.add("第" + index + "组：运行错误 " + safeMsg(runResult.getMessage()));
                    return new JudgeOutcome("RE", passed, testCases.size(), messages);
                }

                String actual = normalize(runResult.getStdout());
                String expected = normalize(testCase.getExpectedOutput());
                if (!expected.equals(actual)) {
                    messages.add("第" + index + "组：答案错误");
                    return new JudgeOutcome("WA", passed, testCases.size(), messages);
                }
                passed++;
                messages.add("第" + index + "组：通过");
            }
            return new JudgeOutcome("AC", passed, testCases.size(), messages);
        } catch (Exception ex) {
            return JudgeOutcome.fail("RE", List.of("本地判题异常：" + safeMsg(ex.getMessage())), 0, testCases.size());
        } finally {
            if (workDir != null) {
                tryDeleteRecursively(workDir);
            }
        }
    }

    private ProgramRunner buildRunner(Integer languageId, String sourceCode, Path workDir) throws IOException {
        if (languageId == null) {
            throw new IllegalArgumentException("languageId 不能为空");
        }
        // 兼容常见语言 ID：50=C，54=C++，71=Python
        if (languageId == 71) {
            Path source = workDir.resolve("main.py");
            Files.writeString(source, sourceCode, StandardCharsets.UTF_8);
            return new PythonRunner(properties, source);
        }
        if (languageId == 50) {
            Path source = workDir.resolve("main.c");
            Files.writeString(source, sourceCode, StandardCharsets.UTF_8);
            Path exe = workDir.resolve(binaryName("main"));
            return new CRunner(properties, source, exe);
        }
        if (languageId == 54) {
            Path source = workDir.resolve("main.cpp");
            Files.writeString(source, sourceCode, StandardCharsets.UTF_8);
            Path exe = workDir.resolve(binaryName("main"));
            return new CppRunner(properties, source, exe);
        }
        throw new IllegalArgumentException("暂不支持的 languageId：" + languageId + "，仅支持 C(50)/C++(54)/Python(71)");
    }

    private static String binaryName(String name) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win") ? name + ".exe" : name;
    }

    private String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\r\n", "\n").trim();
    }

    private String safeMsg(String msg) {
        if (msg == null || msg.isBlank()) {
            return "";
        }
        if (msg.length() <= 200) {
            return msg;
        }
        return msg.substring(0, 200) + "...";
    }

    private void tryDeleteRecursively(Path path) {
        try {
            Files.walk(path)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private interface ProgramRunner {
        CompileResult compile() throws Exception;

        RunResult run(String input) throws Exception;
    }

    private static class PythonRunner implements ProgramRunner {
        private final LocalJudgeProperties properties;
        private final Path source;

        private PythonRunner(LocalJudgeProperties properties, Path source) {
            this.properties = properties;
            this.source = source;
        }

        @Override
        public CompileResult compile() {
            return CompileResult.success();
        }

        @Override
        public RunResult run(String input) throws Exception {
            List<String> command = List.of(properties.getPythonCommand(), source.getFileName().toString());
            return runProcess(command, source.getParent(), input, properties.getTimeoutMs(), properties.getMaxOutputLength());
        }
    }

    private static class CRunner implements ProgramRunner {
        private final LocalJudgeProperties properties;
        private final Path source;
        private final Path exe;

        private CRunner(LocalJudgeProperties properties, Path source, Path exe) {
            this.properties = properties;
            this.source = source;
            this.exe = exe;
        }

        @Override
        public CompileResult compile() throws Exception {
            List<String> compileCmd = List.of(
                    properties.getGccCommand(),
                    source.getFileName().toString(),
                    "-O2",
                    "-std=c11",
                    "-o",
                    exe.getFileName().toString()
            );
            RunResult result = runProcess(compileCmd, source.getParent(), "", properties.getTimeoutMs(), properties.getMaxOutputLength());
            if (!result.isSuccess()) {
                return CompileResult.fail(result.getStderr().isBlank() ? result.getStdout() : result.getStderr());
            }
            return CompileResult.success();
        }

        @Override
        public RunResult run(String input) throws Exception {
            List<String> runCmd = List.of(exe.toAbsolutePath().toString());
            return runProcess(runCmd, source.getParent(), input, properties.getTimeoutMs(), properties.getMaxOutputLength());
        }
    }

    private static class CppRunner implements ProgramRunner {
        private final LocalJudgeProperties properties;
        private final Path source;
        private final Path exe;

        private CppRunner(LocalJudgeProperties properties, Path source, Path exe) {
            this.properties = properties;
            this.source = source;
            this.exe = exe;
        }

        @Override
        public CompileResult compile() throws Exception {
            List<String> compileCmd = List.of(
                    properties.getGppCommand(),
                    source.getFileName().toString(),
                    "-O2",
                    "-std=c++17",
                    "-o",
                    exe.getFileName().toString()
            );
            RunResult result = runProcess(compileCmd, source.getParent(), "", properties.getTimeoutMs(), properties.getMaxOutputLength());
            if (!result.isSuccess()) {
                return CompileResult.fail(result.getStderr().isBlank() ? result.getStdout() : result.getStderr());
            }
            return CompileResult.success();
        }

        @Override
        public RunResult run(String input) throws Exception {
            List<String> runCmd = List.of(exe.toAbsolutePath().toString());
            return runProcess(runCmd, source.getParent(), input, properties.getTimeoutMs(), properties.getMaxOutputLength());
        }
    }

    private static RunResult runProcess(List<String> command,
                                        Path workDir,
                                        String stdin,
                                        long timeoutMs,
                                        int maxOutputLen) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = pb.start();

        CompletableFuture<String> stdoutFuture = readAsync(process.getInputStream(), maxOutputLen);
        CompletableFuture<String> stderrFuture = readAsync(process.getErrorStream(), maxOutputLen);

        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            process.getOutputStream().close();
        }

        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            return RunResult.timeout();
        }

        String stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
        String stderr = stderrFuture.get(1, TimeUnit.SECONDS);
        int exitCode = process.exitValue();
        if (exitCode == 0) {
            return RunResult.success(stdout, stderr);
        }
        return RunResult.fail(stdout, stderr, "exitCode=" + exitCode);
    }

    private static CompletableFuture<String> readAsync(InputStream inputStream, int maxOutputLen) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                int total = 0;
                while ((len = in.read(buffer)) != -1) {
                    if (total + len > maxOutputLen) {
                        out.write(buffer, 0, Math.max(0, maxOutputLen - total));
                        break;
                    }
                    out.write(buffer, 0, len);
                    total += len;
                }
                return out.toString(StandardCharsets.UTF_8);
            } catch (IOException ex) {
                return "";
            }
        });
    }

    @Data
    public static class TestCaseInput {
        private String input;
        private String expectedOutput;
    }

    @Data
    public static class JudgeOutcome {
        private String result;
        private Integer passed;
        private Integer total;
        private List<String> messages;

        public JudgeOutcome(String result, Integer passed, Integer total, List<String> messages) {
            this.result = result;
            this.passed = passed;
            this.total = total;
            this.messages = messages;
        }

        public static JudgeOutcome fail(String result, List<String> messages, int passed, int total) {
            return new JudgeOutcome(result, passed, total, messages);
        }
    }

    @Data
    private static class CompileResult {
        private boolean success;
        private String message;

        public static CompileResult success() {
            CompileResult r = new CompileResult();
            r.success = true;
            r.message = "";
            return r;
        }

        public static CompileResult fail(String msg) {
            CompileResult r = new CompileResult();
            r.success = false;
            r.message = msg == null ? "" : msg;
            return r;
        }
    }

    @Data
    private static class RunResult {
        private boolean success;
        private boolean timeout;
        private String stdout;
        private String stderr;
        private String message;

        public static RunResult success(String stdout, String stderr) {
            RunResult r = new RunResult();
            r.success = true;
            r.timeout = false;
            r.stdout = stdout == null ? "" : stdout;
            r.stderr = stderr == null ? "" : stderr;
            r.message = "";
            return r;
        }

        public static RunResult fail(String stdout, String stderr, String message) {
            RunResult r = new RunResult();
            r.success = false;
            r.timeout = false;
            r.stdout = stdout == null ? "" : stdout;
            r.stderr = stderr == null ? "" : stderr;
            r.message = message == null ? "" : message;
            return r;
        }

        public static RunResult timeout() {
            RunResult r = new RunResult();
            r.success = false;
            r.timeout = true;
            r.stdout = "";
            r.stderr = "";
            r.message = "timeout";
            return r;
        }
    }
}

