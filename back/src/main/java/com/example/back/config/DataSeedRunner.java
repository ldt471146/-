package com.example.back.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.back.entity.EduChapter;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduLesson;
import com.example.back.mapper.EduChapterMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.mapper.EduQuestionOptionMapper;
import com.example.back.mapper.EduLessonMapper;
import com.example.back.mapper.EduCodeProblemMapper;
import com.example.back.mapper.EduCodeTestcaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 初始化课程示例数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeedRunner implements ApplicationRunner {

    private final EduCourseMapper courseMapper;
    private final EduChapterMapper chapterMapper;
    private final EduLessonMapper lessonMapper;
    private final EduQuestionMapper questionMapper;
    private final EduQuestionOptionMapper optionMapper;
    private final EduCodeProblemMapper codeProblemMapper;
    private final EduCodeTestcaseMapper codeTestcaseMapper;

    private Long insertCourse(String title, String intro) {
        EduCourse course = new EduCourse();
        course.setTitle(title);
        course.setIntro(intro);
        course.setCover(null);
        course.setTeacherId(1L);
        course.setStatus(1);
        courseMapper.insert(course);
        return course.getId();
    }

    private void seedPythonCourse(Long courseId) {
        Long c1 = insertChapter(courseId, "第 1 章 认识编程", 1);
        Long c2 = insertChapter(courseId, "第 2 章 变量与条件", 2);
        Long c3 = insertChapter(courseId, "第 3 章 循环与小项目", 3);

        insertLessons(c1, Arrays.asList(
                lesson("什么是编程", "text", null, "认识编程与程序的作用。", 1),
                lesson("搭建开发环境", "text", null, "安装与运行 Python，完成第一行代码。", 2)
        ));
        insertLessons(c2, Arrays.asList(
                lesson("变量与类型", "text", null, "学会使用变量保存信息。", 1),
                lesson("条件判断", "text", null, "掌握 if/else 的基本用法。", 2)
        ));
        insertLessons(c3, Arrays.asList(
                lesson("循环基础", "text", null, "学习 for/while 循环。", 1),
                lesson("制作猜数字小游戏", "text", null, "综合运用变量、条件与循环。", 2)
        ));
    }

    private void seedWebCourse(Long courseId) {
        Long c1 = insertChapter(courseId, "第 1 章 HTML 结构", 1);
        Long c2 = insertChapter(courseId, "第 2 章 CSS 设计", 2);
        Long c3 = insertChapter(courseId, "第 3 章 JavaScript 交互", 3);

        insertLessons(c1, Arrays.asList(
                lesson("HTML 标签速览", "text", null, "认识标题、段落、列表与图片。", 1),
                lesson("页面结构实践", "text", null, "搭建你的第一张网页。", 2)
        ));
        insertLessons(c2, Arrays.asList(
                lesson("颜色与排版", "text", null, "用 CSS 打造清爽布局。", 1),
                lesson("卡片组件", "text", null, "完成课程卡片样式。", 2)
        ));
        insertLessons(c3, Arrays.asList(
                lesson("变量与事件", "text", null, "让页面响应用户操作。", 1),
                lesson("小游戏按钮", "text", null, "实现点击计数小功能。", 2)
        ));
    }

    private void seedAlgoCourse(Long courseId) {
        Long c1 = insertChapter(courseId, "第 1 章 图形化算法", 1);
        Long c2 = insertChapter(courseId, "第 2 章 逻辑与步骤", 2);
        Long c3 = insertChapter(courseId, "第 3 章 练习与挑战", 3);

        insertLessons(c1, Arrays.asList(
                lesson("路径规划", "text", null, "用迷宫理解路径算法。", 1),
                lesson("排序体验", "text", null, "用卡片模拟排序过程。", 2)
        ));
        insertLessons(c2, Arrays.asList(
                lesson("分解问题", "text", null, "学会把问题拆成小步骤。", 1),
                lesson("条件与循环", "text", null, "用日常生活理解逻辑判断。", 2)
        ));
        insertLessons(c3, Arrays.asList(
                lesson("挑战：最短路线", "text", null, "综合练习算法思维。", 1),
                lesson("挑战：基础竞赛题", "text", null, "体验简单算法题。", 2)
        ));

        seedQuestions(courseId, c1, c2, c3);
    }

    private Long insertChapter(Long courseId, String title, int sortNo) {
        EduChapter chapter = new EduChapter();
        chapter.setCourseId(courseId);
        chapter.setTitle(title);
        chapter.setSortNo(sortNo);
        chapterMapper.insert(chapter);
        return chapter.getId();
    }

    private void insertLessons(Long chapterId, List<EduLesson> lessons) {
        for (EduLesson lesson : lessons) {
            lesson.setChapterId(chapterId);
            lessonMapper.insert(lesson);
        }
    }

    private EduLesson lesson(String title, String contentType, String contentUrl, String contentText, int sortNo) {
        EduLesson lesson = new EduLesson();
        lesson.setTitle(title);
        lesson.setContentType(contentType);
        lesson.setContentUrl(contentUrl);
        lesson.setContentText(contentText);
        lesson.setSortNo(sortNo);
        return lesson;
    }

    private void seedQuestions(Long courseId, Long chapter1, Long chapter2, Long chapter3) {
        if (questionMapper.selectCount(null) != null && questionMapper.selectCount(null) > 0) {
            return;
        }
        Long q1 = insertQuestion(courseId, chapter1, "Python 中用于输出内容的函数是？", "single", 1,
                "print() 是 Python 的输出函数。");
        insertOptions(q1, "A", "print()", 1);
        insertOptions(q1, "B", "echo()", 0);
        insertOptions(q1, "C", "console()", 0);
        insertOptions(q1, "D", "show()", 0);

        Long q2 = insertQuestion(courseId, chapter2, "以下哪些属于前端三件套？", "multi", 1,
                "HTML、CSS、JavaScript 是前端基础。");
        insertOptions(q2, "A", "HTML", 1);
        insertOptions(q2, "B", "CSS", 1);
        insertOptions(q2, "C", "JavaScript", 1);
        insertOptions(q2, "D", "Python", 0);

        Long q3 = insertQuestion(courseId, chapter3, "算法思维强调的能力是？", "single", 2,
                "算法思维强调逻辑与步骤分解。");
        insertOptions(q3, "A", "记忆力", 0);
        insertOptions(q3, "B", "逻辑思维", 1);
        insertOptions(q3, "C", "听力", 0);
        insertOptions(q3, "D", "拼写", 0);
    }

    private void seedCodeProblems() {
        if (codeProblemMapper.selectCount(null) != null && codeProblemMapper.selectCount(null) > 0) {
            return;
        }

        Long p1 = insertCodeProblem(
                "A+B 问题",
                "给定两个整数 a 和 b，输出它们的和。\n\n输入：一行两个整数 a、b（-10^9 <= a,b <= 10^9）\n输出：a+b",
                1, 1000, 256
        );
        insertCodeTestcase(p1, "1 2\n", "3\n", 1);
        insertCodeTestcase(p1, "100 -50\n", "50\n", 0);

        Long p2 = insertCodeProblem(
                "字符串反转",
                "输入一行字符串，输出其反转结果。\n\n输入：一行字符串 s\n输出：反转后的字符串",
                1, 1000, 256
        );
        insertCodeTestcase(p2, "hello\n", "olleh\n", 1);
        insertCodeTestcase(p2, "abcd\n", "dcba\n", 0);
    }

    private Long insertQuestion(Long courseId, Long chapterId, String title, String type, int difficulty, String analysis) {
        com.example.back.entity.EduQuestion q = new com.example.back.entity.EduQuestion();
        q.setCourseId(courseId);
        q.setChapterId(chapterId);
        q.setTitle(title);
        q.setType(type);
        q.setDifficulty(difficulty);
        q.setAnalysis(analysis);
        questionMapper.insert(q);
        return q.getId();
    }

    private void insertOptions(Long questionId, String label, String content, int isCorrect) {
        com.example.back.entity.EduQuestionOption opt = new com.example.back.entity.EduQuestionOption();
        opt.setQuestionId(questionId);
        opt.setLabel(label);
        opt.setContent(content);
        opt.setIsCorrect(isCorrect);
        optionMapper.insert(opt);
    }

    private Long insertCodeProblem(String title, String content, int difficulty, int timeLimit, int memoryLimit) {
        com.example.back.entity.EduCodeProblem p = new com.example.back.entity.EduCodeProblem();
        p.setTitle(title);
        p.setContent(content);
        p.setDifficulty(difficulty);
        p.setTimeLimit(timeLimit);
        p.setMemoryLimit(memoryLimit);
        p.setStatus(1);
        codeProblemMapper.insert(p);
        return p.getId();
    }

    private void insertCodeTestcase(Long problemId, String input, String output, int isSample) {
        com.example.back.entity.EduCodeTestcase tc = new com.example.back.entity.EduCodeTestcase();
        tc.setProblemId(problemId);
        tc.setInputData(input);
        tc.setOutputData(output);
        tc.setIsSample(isSample);
        codeTestcaseMapper.insert(tc);
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = courseMapper.selectCount(Wrappers.lambdaQuery());
        if (count != null && count > 0) {
            seedCodeProblems();
            return;
        }

        Long courseId1 = insertCourse(
                "Python 入门：从零到小游戏",
                "适合青少年的 Python 入门课程，循序渐进掌握基础语法与小项目。"
        );
        Long courseId2 = insertCourse(
                "Web 前端三件套：HTML/CSS/JS",
                "从页面结构到交互效果，做出第一个酷炫网页。"
        );
        Long courseId3 = insertCourse(
                "算法思维启蒙：从图形到逻辑",
                "用游戏和图形理解算法思维，培养逻辑能力。"
        );

        seedPythonCourse(courseId1);
        seedWebCourse(courseId2);
        seedAlgoCourse(courseId3);

        seedCodeProblems();

        log.info("课程示例数据已初始化");
    }
}
