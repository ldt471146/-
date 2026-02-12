/*
 Navicat Premium Data Transfer

 Source Server         : 我的数据库
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : online

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 12/02/2026 14:45:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for edu_chapter
-- ----------------------------
DROP TABLE IF EXISTS `edu_chapter`;
CREATE TABLE `edu_chapter`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(0) NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort_no` int(0) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_course`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_chapter
-- ----------------------------
INSERT INTO `edu_chapter` VALUES (11, 1, '第 1 章 认识编程', 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (12, 1, '第 2 章 变量与条件', 2, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (13, 1, '第 3 章 循环与小项目', 3, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (21, 2, '第 1 章 HTML 结构', 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (22, 2, '第 2 章 CSS 设计', 2, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (23, 2, '第 3 章 JavaScript 交互', 3, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (31, 3, '第 1 章 图形化算法', 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (32, 3, '第 2 章 逻辑与步骤', 2, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (33, 3, '第 3 章 练习与挑战', 3, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0);
INSERT INTO `edu_chapter` VALUES (34, 5, '基础入门', 1, '2026-02-08 14:16:07', '2026-02-08 14:16:07', 0);
INSERT INTO `edu_chapter` VALUES (35, 7, 'java基础知识学习', 1, '2026-02-12 12:42:37', '2026-02-12 12:42:37', 0);
INSERT INTO `edu_chapter` VALUES (36, 7, 'java高级语法', 2, '2026-02-12 12:43:02', '2026-02-12 12:43:02', 0);

-- ----------------------------
-- Table structure for edu_class
-- ----------------------------
DROP TABLE IF EXISTS `edu_class`;
CREATE TABLE `edu_class`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `teacher_id` bigint(0) NOT NULL,
  `grade` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for edu_class_member
-- ----------------------------
DROP TABLE IF EXISTS `edu_class_member`;
CREATE TABLE `edu_class_member`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(0) NOT NULL,
  `student_id` bigint(0) NOT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_student`(`class_id`, `student_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for edu_code_problem
-- ----------------------------
DROP TABLE IF EXISTS `edu_code_problem`;
CREATE TABLE `edu_code_problem`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `difficulty` int(0) NULL DEFAULT 1,
  `time_limit` int(0) NULL DEFAULT 1000,
  `memory_limit` int(0) NULL DEFAULT 256,
  `status` tinyint(0) NULL DEFAULT 1,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  `teacher_id` bigint(0) NULL DEFAULT NULL COMMENT '归属教师',
  `course_id` bigint(0) NULL DEFAULT NULL COMMENT '归属课程',
  `chapter_id` bigint(0) NULL DEFAULT NULL COMMENT '归属章节',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_code_problem
-- ----------------------------
INSERT INTO `edu_code_problem` VALUES (1, 'A+B 问题', '给定两个整数 a 和 b，输出它们的和。\n\n输入：一行两个整数 a、b（-10^9 <= a,b <= 10^9）\n输出：a+b', 1, 1000, 256, 1, '2026-02-06 10:53:12', '2026-02-06 10:53:12', 0, NULL, NULL, NULL);
INSERT INTO `edu_code_problem` VALUES (2, '字符串反转', '输入一行字符串，输出其反转结果。\n\n输入：一行字符串 s\n输出：反转后的字符串', 1, 1000, 256, 1, '2026-02-06 10:53:13', '2026-02-06 10:53:13', 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for edu_code_submission
-- ----------------------------
DROP TABLE IF EXISTS `edu_code_submission`;
CREATE TABLE `edu_code_submission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `problem_id` bigint(0) NOT NULL,
  `language_id` int(0) NOT NULL,
  `source_code` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `result` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `passed_count` int(0) NULL DEFAULT 0,
  `total_count` int(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_code_submission
-- ----------------------------
INSERT INTO `edu_code_submission` VALUES (1, 1, 2, 50, '// 在这里编写你的代码\n#include <bits/stdc++.h>\nusing namespace std;\n\nint main(){\n    ios::sync_with_stdio(false);\n    cin.tie(nullptr);\n\n    long long a,b;\n    if(!(cin>>a>>b)) return 0;\n    cout<<a+b;\n    return 0;\n}\n', 'CE', 0, 2, '2026-02-12 12:16:49', '2026-02-12 12:16:49', 0);
INSERT INTO `edu_code_submission` VALUES (2, 1, 2, 50, '#include <iostream>\n#include <string>\n#include <algorithm>\nusing namespace std;\n\nint main() {\n    string s;\n    getline(cin, s);\n    reverse(s.begin(), s.end());\n    cout << s;\n    return 0;\n}\n', 'CE', 0, 2, '2026-02-12 12:17:27', '2026-02-12 12:17:27', 0);
INSERT INTO `edu_code_submission` VALUES (3, 1, 2, 50, '#include <iostream>\n#include <string>\n#include <algorithm>\nusing namespace std;\n\nint main() {\n    string s;\n    getline(cin, s);\n    reverse(s.begin(), s.end());\n    cout << s;\n    return 0;\n}\n', 'CE', 0, 2, '2026-02-12 12:17:28', '2026-02-12 12:17:28', 0);
INSERT INTO `edu_code_submission` VALUES (4, 1, 2, 54, '#include <iostream>\n#include <string>\n#include <algorithm>\nusing namespace std;\n\nint main() {\n    string s;\n    getline(cin, s);\n    reverse(s.begin(), s.end());\n    cout << s;\n    return 0;\n}\n', 'CE', 0, 2, '2026-02-12 12:18:00', '2026-02-12 12:18:00', 0);
INSERT INTO `edu_code_submission` VALUES (5, 1, 2, 54, '#include <iostream>\n#include <string>\n#include <algorithm>\nusing namespace std;\n\nint main() {\n    string s;\n    getline(cin, s);\n    reverse(s.begin(), s.end());\n    cout << s;\n    return 0;\n}\n', 'AC', 2, 2, '2026-02-12 12:23:31', '2026-02-12 12:23:31', 0);

-- ----------------------------
-- Table structure for edu_code_testcase
-- ----------------------------
DROP TABLE IF EXISTS `edu_code_testcase`;
CREATE TABLE `edu_code_testcase`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `problem_id` bigint(0) NOT NULL,
  `input_data` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `output_data` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `is_sample` tinyint(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_code_testcase
-- ----------------------------
INSERT INTO `edu_code_testcase` VALUES (1, 1, '1 2\n', '3\n', 1, '2026-02-06 10:53:12', '2026-02-06 10:53:12', 0);
INSERT INTO `edu_code_testcase` VALUES (2, 1, '100 -50\n', '50\n', 0, '2026-02-06 10:53:12', '2026-02-06 10:53:12', 0);
INSERT INTO `edu_code_testcase` VALUES (3, 2, 'hello\n', 'olleh\n', 1, '2026-02-06 10:53:13', '2026-02-06 10:53:13', 0);
INSERT INTO `edu_code_testcase` VALUES (4, 2, 'abcd\n', 'dcba\n', 0, '2026-02-06 10:53:13', '2026-02-06 10:53:13', 0);

-- ----------------------------
-- Table structure for edu_course
-- ----------------------------
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `teacher_id` bigint(0) NOT NULL,
  `status` tinyint(0) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  `finish_status` tinyint(0) NULL DEFAULT 0 COMMENT '完结状态：1已完结 0更新中',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course
-- ----------------------------
INSERT INTO `edu_course` VALUES (1, 'Python 入门：从零到小游戏', NULL, '适合青少年的 Python 入门课程，循序渐进掌握基础语法与小项目。', 1, 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0, 0);
INSERT INTO `edu_course` VALUES (2, 'Web 前端三件套：HTML/CSS/JS', NULL, '从页面结构到交互效果，做出第一个酷炫网页。', 1, 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0, 0);
INSERT INTO `edu_course` VALUES (3, '算法思维启蒙：从图形到逻辑', NULL, '用游戏和图形理解算法思维，培养逻辑能力。', 1, 1, '2026-02-05 23:25:52', '2026-02-05 23:25:52', 0, 0);
INSERT INTO `edu_course` VALUES (4, '前后端分离项目', 'https://i0.hdslb.com/bfs/article/52f31fc44a730f55b9906a24256cf21c5c830634.jpg@1256w_838h_!web-article-pic.avif', '好好学习前后端分离项目', 9, 1, '2026-02-06 15:54:56', '2026-02-06 15:54:56', 0, 0);
INSERT INTO `edu_course` VALUES (5, 'python', 'https://pixnio.com/free-images/2025/09/30/2025-09-30-16-13-06-576x720.jpeg', '好好学习天天向上', 3, 1, '2026-02-08 10:15:56', '2026-02-08 14:31:18', 1, 0);
INSERT INTO `edu_course` VALUES (6, '二次元学习', 'https://i0.hdslb.com/bfs/article/3fe50ff428ab553dfcc38d8c5d75ca4bbb22abd7.jpg@1256w_686h_!web-article-pic.avif', '213123', 3, 1, '2026-02-08 11:34:42', '2026-02-08 11:43:37', 1, 0);
INSERT INTO `edu_course` VALUES (7, 'java学习', '', '', 3, 1, '2026-02-08 15:18:06', '2026-02-08 15:18:06', 0, 0);

-- ----------------------------
-- Table structure for edu_course_enroll
-- ----------------------------
DROP TABLE IF EXISTS `edu_course_enroll`;
CREATE TABLE `edu_course_enroll`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `course_id` bigint(0) NOT NULL,
  `status` tinyint(0) NULL DEFAULT 1,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course_enroll
-- ----------------------------
INSERT INTO `edu_course_enroll` VALUES (1, 1, 2, 1, '2026-02-06 09:27:22', '2026-02-06 09:27:22', 0);
INSERT INTO `edu_course_enroll` VALUES (2, 1, 3, 1, '2026-02-06 09:48:22', '2026-02-06 09:48:22', 0);
INSERT INTO `edu_course_enroll` VALUES (3, 3, 5, 0, '2026-02-08 11:44:33', '2026-02-08 11:44:33', 0);
INSERT INTO `edu_course_enroll` VALUES (4, 1, 5, 1, '2026-02-08 14:13:54', '2026-02-08 14:13:54', 0);
INSERT INTO `edu_course_enroll` VALUES (5, 1, 7, 1, '2026-02-08 15:29:15', '2026-02-08 15:29:15', 0);

-- ----------------------------
-- Table structure for edu_exam_submission
-- ----------------------------
DROP TABLE IF EXISTS `edu_exam_submission`;
CREATE TABLE `edu_exam_submission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  `total_count` int(0) NOT NULL,
  `correct_count` int(0) NOT NULL,
  `score` int(0) NOT NULL,
  `detail_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `submitted_at` datetime(0) NOT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_submission_user`(`user_id`) USING BTREE,
  INDEX `idx_exam_submission_task`(`task_id`) USING BTREE,
  INDEX `idx_exam_submission_time`(`submitted_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_exam_submission
-- ----------------------------
INSERT INTO `edu_exam_submission` VALUES (1, 1, 1, 10, 2, 20, '[{\"questionId\":16,\"title\":\"二分查找适用的前提是？\",\"correct\":false,\"userAnswers\":[\"B\"],\"correctAnswers\":[\"A\"],\"analysis\":\"二分查找要求序列有序。\"},{\"questionId\":12,\"title\":\"if 语句主要用于实现什么逻辑？\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"B\"],\"analysis\":\"if 用于条件分支判断。\"},{\"questionId\":13,\"title\":\"算法思维强调的核心能力有哪些？\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"A\",\"B\",\"C\"],\"analysis\":\"包括分解问题、抽象规律、设计步骤。\"},{\"questionId\":17,\"title\":\"在流程图中，判断（条件）节点常用的图形是？\",\"correct\":true,\"userAnswers\":[\"B\"],\"correctAnswers\":[\"B\"],\"analysis\":\"判断节点通常用菱形表示。\"},{\"questionId\":14,\"title\":\"以下哪个时间复杂度通常比 O(n) 更快（n 足够大时）？\",\"correct\":true,\"userAnswers\":[\"B\"],\"correctAnswers\":[\"B\"],\"analysis\":\"O(log n) 增长更慢，通常更快。\"},{\"questionId\":18,\"title\":\"下面哪些属于良好的代码习惯？\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"A\",\"B\",\"C\"],\"analysis\":\"命名清晰、适量注释、模块化是常见实践。\"},{\"questionId\":19,\"title\":\"调试代码时，以下做法更合理的是？\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"B\"],\"analysis\":\"定位问题应先复现，再逐步缩小范围。\"},{\"questionId\":7,\"title\":\"1+1=\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"D\"],\"analysis\":\"\"},{\"questionId\":10,\"title\":\"变量的主要作用是？\",\"correct\":false,\"userAnswers\":[\"C\"],\"correctAnswers\":[\"B\"],\"analysis\":\"变量用于存储可变化的数据。\"},{\"questionId\":9,\"title\":\"以下哪些属于前端三件套？\",\"correct\":false,\"userAnswers\":[\"B\"],\"correctAnswers\":[\"A\",\"B\",\"C\"],\"analysis\":\"前端基础技术为 HTML、CSS、JavaScript。\"}]', '2026-02-08 15:29:55', '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);

-- ----------------------------
-- Table structure for edu_exam_task
-- ----------------------------
DROP TABLE IF EXISTS `edu_exam_task`;
CREATE TABLE `edu_exam_task`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint(0) NOT NULL,
  `course_id` bigint(0) NOT NULL,
  `chapter_id` bigint(0) NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `question_count` int(0) NOT NULL DEFAULT 10,
  `duration_minutes` int(0) NOT NULL DEFAULT 30,
  `start_time` datetime(0) NOT NULL,
  `end_time` datetime(0) NULL DEFAULT NULL,
  `status` tinyint(0) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_task_teacher`(`teacher_id`) USING BTREE,
  INDEX `idx_exam_task_course`(`course_id`) USING BTREE,
  INDEX `idx_exam_task_status`(`status`) USING BTREE,
  INDEX `idx_exam_task_start_time`(`start_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_exam_task
-- ----------------------------
INSERT INTO `edu_exam_task` VALUES (1, 3, 7, NULL, '第一章测试', 10, 30, '2026-02-08 00:00:00', '2026-02-09 00:00:00', 1, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);

-- ----------------------------
-- Table structure for edu_exam_task_question
-- ----------------------------
DROP TABLE IF EXISTS `edu_exam_task_question`;
CREATE TABLE `edu_exam_task_question`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(0) NOT NULL,
  `question_id` bigint(0) NOT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_exam_task_question`(`task_id`, `question_id`) USING BTREE,
  INDEX `idx_exam_task_question_task`(`task_id`) USING BTREE,
  INDEX `idx_exam_task_question_q`(`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_exam_task_question
-- ----------------------------
INSERT INTO `edu_exam_task_question` VALUES (1, 1, 16, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (2, 1, 12, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (3, 1, 13, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (4, 1, 17, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (5, 1, 14, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (6, 1, 18, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (7, 1, 19, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (8, 1, 7, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (9, 1, 10, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);
INSERT INTO `edu_exam_task_question` VALUES (10, 1, 9, '2026-02-08 15:28:45', '2026-02-08 15:28:45', 0);

-- ----------------------------
-- Table structure for edu_homework
-- ----------------------------
DROP TABLE IF EXISTS `edu_homework`;
CREATE TABLE `edu_homework`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(0) NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `deadline` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for edu_homework_problem
-- ----------------------------
DROP TABLE IF EXISTS `edu_homework_problem`;
CREATE TABLE `edu_homework_problem`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `homework_id` bigint(0) NOT NULL,
  `problem_id` bigint(0) NOT NULL,
  `score` int(0) NOT NULL DEFAULT 100,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hw_problem`(`homework_id`, `problem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for edu_learn_record
-- ----------------------------
DROP TABLE IF EXISTS `edu_learn_record`;
CREATE TABLE `edu_learn_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `lesson_id` bigint(0) NOT NULL,
  `progress` int(0) NOT NULL DEFAULT 0,
  `is_finished` tinyint(0) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  `learn_seconds` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_lesson`(`user_id`, `lesson_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_learn_record
-- ----------------------------
INSERT INTO `edu_learn_record` VALUES (1, 1, 301, 100, 1, '2026-02-05 23:31:06', '2026-02-05 23:31:06', 0, 0);
INSERT INTO `edu_learn_record` VALUES (2, 1, 103, 30, 0, '2026-02-06 09:09:48', '2026-02-06 09:09:48', 0, 0);
INSERT INTO `edu_learn_record` VALUES (3, 1, 101, 100, 1, '2026-02-06 09:16:36', '2026-02-06 09:16:36', 0, 0);
INSERT INTO `edu_learn_record` VALUES (4, 1, 201, 100, 1, '2026-02-06 11:42:27', '2026-02-06 11:42:27', 0, 3);
INSERT INTO `edu_learn_record` VALUES (5, 1, 302, 100, 1, '2026-02-06 13:02:48', '2026-02-06 13:02:48', 0, 0);

-- ----------------------------
-- Table structure for edu_lesson
-- ----------------------------
DROP TABLE IF EXISTS `edu_lesson`;
CREATE TABLE `edu_lesson`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint(0) NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content_text` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `sort_no` int(0) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lesson_chapter`(`chapter_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 307 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_lesson
-- ----------------------------
INSERT INTO `edu_lesson` VALUES (101, 11, '什么是编程', 'text', NULL, '认识编程与程序的作用。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (102, 11, '搭建开发环境', 'text', NULL, '安装与运行 Python，完成第一行代码。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (103, 12, '变量与类型', 'text', NULL, '学会使用变量保存信息。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (104, 12, '条件判断', 'text', NULL, '掌握 if/else 的基本用法。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (105, 13, '循环基础', 'text', NULL, '学习 for/while 循环。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (106, 13, '制作猜数字小游戏', 'text', NULL, '综合运用变量、条件与循环。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (201, 21, 'HTML 标签速览', 'text', NULL, '认识标题、段落、列表与图片。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (202, 21, '页面结构实践', 'text', NULL, '搭建你的第一张网页。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (203, 22, '颜色与排版', 'text', NULL, '用 CSS 打造清爽布局。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (204, 22, '卡片组件', 'text', NULL, '完成课程卡片样式。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (205, 23, '变量与事件', 'text', NULL, '让页面响应用户操作。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (206, 23, '小游戏按钮', 'text', NULL, '实现点击计数小功能。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (301, 31, '路径规划', 'text', NULL, '用迷宫理解路径算法。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (302, 31, '排序体验', 'text', NULL, '用卡片模拟排序过程。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (303, 32, '分解问题', 'text', NULL, '学会把问题拆成小步骤。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (304, 32, '条件与循环', 'text', NULL, '用日常生活理解逻辑判断。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (305, 33, '挑战：最短路线', 'text', NULL, '综合练习算法思维。', 1, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);
INSERT INTO `edu_lesson` VALUES (306, 33, '挑战：基础竞赛题', 'text', NULL, '体验简单算法题。', 2, '2026-02-05 23:25:53', '2026-02-05 23:25:53', 0);

-- ----------------------------
-- Table structure for edu_question
-- ----------------------------
DROP TABLE IF EXISTS `edu_question`;
CREATE TABLE `edu_question`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `analysis` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `difficulty` int(0) NULL DEFAULT 1,
  `course_id` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  `chapter_id` bigint(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_question
-- ----------------------------
INSERT INTO `edu_question` VALUES (1, 'Python 中用于输出内容的函数是？', 'single', 'print() 是 Python 的输出函数。', 1, 1, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 11);
INSERT INTO `edu_question` VALUES (2, '以下哪些是 Python 的数据类型？', 'multi', 'int、str、list 都是 Python 常见数据类型。', 1, 1, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 11);
INSERT INTO `edu_question` VALUES (3, 'HTML 的作用是？', 'single', 'HTML 负责页面结构。', 1, 2, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 21);
INSERT INTO `edu_question` VALUES (4, '以下哪些属于前端三件套？', 'multi', 'HTML、CSS、JavaScript 是前端三件套。', 1, 2, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 21);
INSERT INTO `edu_question` VALUES (5, '算法思维强调的能力是？', 'single', '算法思维强调逻辑与步骤分解。', 2, 3, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 31);
INSERT INTO `edu_question` VALUES (6, '以下哪些属于排序算法？', 'multi', '冒泡排序、选择排序都是常见排序算法。', 2, 3, '2026-02-06 09:49:44', '2026-02-06 11:38:32', 0, 31);
INSERT INTO `edu_question` VALUES (7, '1+1=', 'single', '', 1, 7, '2026-02-08 15:18:34', '2026-02-12 12:43:54', 1, NULL);
INSERT INTO `edu_question` VALUES (8, 'Python 中用于输出内容的函数是？', 'single', 'print() 是 Python 的标准输出函数。', 1, 7, '2026-02-08 15:25:44', '2026-02-12 12:43:54', 1, NULL);
INSERT INTO `edu_question` VALUES (9, '以下哪些属于前端三件套？', 'multi', '前端基础技术为 HTML、CSS、JavaScript。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:54', 1, NULL);
INSERT INTO `edu_question` VALUES (10, '变量的主要作用是？', 'single', '变量用于存储可变化的数据。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:54', 1, NULL);
INSERT INTO `edu_question` VALUES (11, '下列属于循环结构的是？', 'multi', 'for 和 while 都是循环语句。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1, NULL);
INSERT INTO `edu_question` VALUES (12, 'if 语句主要用于实现什么逻辑？', 'single', 'if 用于条件分支判断。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1, NULL);
INSERT INTO `edu_question` VALUES (13, '算法思维强调的核心能力有哪些？', 'multi', '包括分解问题、抽象规律、设计步骤。', 2, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1, NULL);
INSERT INTO `edu_question` VALUES (14, '以下哪个时间复杂度通常比 O(n) 更快（n 足够大时）？', 'single', 'O(log n) 增长更慢，通常更快。', 2, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1, NULL);
INSERT INTO `edu_question` VALUES (15, '数组的特点通常包括？', 'multi', '数组元素有序、可通过下标访问、在内存中连续存储（抽象层面）。', 2, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1, NULL);
INSERT INTO `edu_question` VALUES (16, '二分查找适用的前提是？', 'single', '二分查找要求序列有序。', 2, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1, NULL);
INSERT INTO `edu_question` VALUES (17, '在流程图中，判断（条件）节点常用的图形是？', 'single', '判断节点通常用菱形表示。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1, NULL);
INSERT INTO `edu_question` VALUES (18, '下面哪些属于良好的代码习惯？', 'multi', '命名清晰、适量注释、模块化是常见实践。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1, NULL);
INSERT INTO `edu_question` VALUES (19, '调试代码时，以下做法更合理的是？', 'single', '定位问题应先复现，再逐步缩小范围。', 1, 7, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1, NULL);

-- ----------------------------
-- Table structure for edu_question_favorite
-- ----------------------------
DROP TABLE IF EXISTS `edu_question_favorite`;
CREATE TABLE `edu_question_favorite`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `question_id` bigint(0) NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id`, `question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_question_favorite
-- ----------------------------
INSERT INTO `edu_question_favorite` VALUES (1, 1, 6, '2026-02-06 11:25:42', '2026-02-06 11:25:42', 0);
INSERT INTO `edu_question_favorite` VALUES (2, 1, 4, '2026-02-06 14:17:38', '2026-02-06 14:17:38', 0);
INSERT INTO `edu_question_favorite` VALUES (3, 1, 3, '2026-02-06 14:17:51', '2026-02-06 14:17:51', 0);

-- ----------------------------
-- Table structure for edu_question_option
-- ----------------------------
DROP TABLE IF EXISTS `edu_question_option`;
CREATE TABLE `edu_question_option`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(0) NOT NULL,
  `label` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `is_correct` tinyint(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_question_option
-- ----------------------------
INSERT INTO `edu_question_option` VALUES (1, 1, 'A', 'print()', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (2, 1, 'B', 'echo()', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (3, 1, 'C', 'console()', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (4, 1, 'D', 'show()', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (5, 2, 'A', 'int', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (6, 2, 'B', 'str', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (7, 2, 'C', 'list', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (8, 2, 'D', 'floaty', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (9, 3, 'A', '定义页面结构', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (10, 3, 'B', '控制数据库', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (11, 3, 'C', '发送邮件', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (12, 3, 'D', '运行服务器', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (13, 4, 'A', 'HTML', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (14, 4, 'B', 'CSS', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (15, 4, 'C', 'JavaScript', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (16, 4, 'D', 'Python', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (17, 5, 'A', '记忆力', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (18, 5, 'B', '逻辑思维', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (19, 5, 'C', '听力', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (20, 5, 'D', '拼写', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (21, 6, 'A', '冒泡排序', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (22, 6, 'B', '选择排序', 1, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (23, 6, 'C', '哈希表', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (24, 6, 'D', '栈', 0, '2026-02-06 09:49:44', '2026-02-06 09:49:44', 0);
INSERT INTO `edu_question_option` VALUES (25, 7, 'A', '1', 0, '2026-02-08 15:18:34', '2026-02-12 12:43:54', 1);
INSERT INTO `edu_question_option` VALUES (26, 7, 'B', '23', 0, '2026-02-08 15:18:34', '2026-02-12 12:43:54', 1);
INSERT INTO `edu_question_option` VALUES (27, 7, 'C', '3', 0, '2026-02-08 15:18:34', '2026-02-12 12:43:54', 1);
INSERT INTO `edu_question_option` VALUES (28, 7, 'D', '2', 1, '2026-02-08 15:18:34', '2026-02-12 12:43:54', 1);
INSERT INTO `edu_question_option` VALUES (29, 8, 'A', 'print()', 1, '2026-02-08 15:25:44', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (30, 8, 'B', 'echo()', 0, '2026-02-08 15:25:44', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (31, 8, 'C', 'show()', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (32, 8, 'D', 'console()', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (33, 9, 'A', 'HTML', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (34, 9, 'B', 'CSS', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (35, 9, 'C', 'JavaScript', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (36, 9, 'D', 'MySQL', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (37, 10, 'A', '画图', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (38, 10, 'B', '存储数据', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (39, 10, 'C', '连接网络', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (40, 10, 'D', '删除文件', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:53', 1);
INSERT INTO `edu_question_option` VALUES (41, 11, 'A', 'for', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1);
INSERT INTO `edu_question_option` VALUES (42, 11, 'B', 'while', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1);
INSERT INTO `edu_question_option` VALUES (43, 11, 'C', 'if', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1);
INSERT INTO `edu_question_option` VALUES (44, 11, 'D', 'break', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:52', 1);
INSERT INTO `edu_question_option` VALUES (45, 12, 'A', '重复执行', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (46, 12, 'B', '条件判断', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (47, 12, 'C', '定义函数', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (48, 12, 'D', '导入模块', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (49, 13, 'A', '问题分解', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (50, 13, 'B', '模式识别', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (51, 13, 'C', '步骤设计', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (52, 13, 'D', '随机猜测', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (53, 14, 'A', 'O(n^2)', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (54, 14, 'B', 'O(log n)', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (55, 14, 'C', 'O(n)', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (56, 14, 'D', 'O(n log n)', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:51', 1);
INSERT INTO `edu_question_option` VALUES (57, 15, 'A', '通过下标访问', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (58, 15, 'B', '元素通常类型一致', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (59, 15, 'C', '天然适合频繁中间插入', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (60, 15, 'D', '有序排列', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (61, 16, 'A', '数据必须有序', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (62, 16, 'B', '数据必须是字符串', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (63, 16, 'C', '数据必须无重复', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (64, 16, 'D', '数据必须是二维数组', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (65, 17, 'A', '矩形', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (66, 17, 'B', '菱形', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (67, 17, 'C', '圆形', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (68, 17, 'D', '平行四边形', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (69, 18, 'A', '变量命名有意义', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (70, 18, 'B', '关键逻辑写注释', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (71, 18, 'C', '函数职责单一', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (72, 18, 'D', '全部代码写在一个函数里', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:50', 1);
INSERT INTO `edu_question_option` VALUES (73, 19, 'A', '不看报错直接重写', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:49', 1);
INSERT INTO `edu_question_option` VALUES (74, 19, 'B', '复现问题并查看日志', 1, '2026-02-08 15:25:45', '2026-02-12 12:43:49', 1);
INSERT INTO `edu_question_option` VALUES (75, 19, 'C', '随机改代码碰碰运气', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:49', 1);
INSERT INTO `edu_question_option` VALUES (76, 19, 'D', '先删掉所有判断语句', 0, '2026-02-08 15:25:45', '2026-02-12 12:43:49', 1);

-- ----------------------------
-- Table structure for edu_question_record
-- ----------------------------
DROP TABLE IF EXISTS `edu_question_record`;
CREATE TABLE `edu_question_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `question_id` bigint(0) NOT NULL,
  `answer` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_correct` tinyint(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_question_record
-- ----------------------------
INSERT INTO `edu_question_record` VALUES (1, 1, 1, 'C', 0, '2026-02-06 09:59:45', '2026-02-06 09:59:45', 0);
INSERT INTO `edu_question_record` VALUES (2, 1, 2, 'C', 0, '2026-02-06 09:59:54', '2026-02-06 09:59:54', 0);
INSERT INTO `edu_question_record` VALUES (3, 1, 3, 'B', 0, '2026-02-06 09:59:57', '2026-02-06 09:59:57', 0);
INSERT INTO `edu_question_record` VALUES (4, 1, 4, 'B', 0, '2026-02-06 10:00:00', '2026-02-06 10:00:00', 0);
INSERT INTO `edu_question_record` VALUES (5, 1, 5, 'B', 1, '2026-02-06 10:00:04', '2026-02-06 10:00:04', 0);
INSERT INTO `edu_question_record` VALUES (6, 1, 6, 'B', 0, '2026-02-06 14:17:23', '2026-02-06 14:17:23', 0);
INSERT INTO `edu_question_record` VALUES (7, 1, 6, 'A,B', 1, '2026-02-06 14:17:26', '2026-02-06 14:17:26', 0);
INSERT INTO `edu_question_record` VALUES (8, 1, 5, 'D', 0, '2026-02-06 14:17:34', '2026-02-06 14:17:34', 0);
INSERT INTO `edu_question_record` VALUES (9, 1, 2, 'B', 0, '2026-02-06 14:26:12', '2026-02-06 14:26:12', 0);
INSERT INTO `edu_question_record` VALUES (10, 1, 1, 'B', 0, '2026-02-06 14:26:22', '2026-02-06 14:26:22', 0);
INSERT INTO `edu_question_record` VALUES (11, 3, 6, 'B', 0, '2026-02-08 11:57:10', '2026-02-08 11:57:10', 0);
INSERT INTO `edu_question_record` VALUES (12, 1, 3, 'C', 0, '2026-02-08 14:14:07', '2026-02-08 14:14:07', 0);
INSERT INTO `edu_question_record` VALUES (13, 3, 5, 'B', 1, '2026-02-08 15:09:07', '2026-02-08 15:09:07', 0);
INSERT INTO `edu_question_record` VALUES (14, 3, 6, 'B', 0, '2026-02-08 15:09:07', '2026-02-08 15:09:07', 0);
INSERT INTO `edu_question_record` VALUES (15, 1, 16, 'B', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (16, 1, 12, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (17, 1, 13, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (18, 1, 17, 'B', 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (19, 1, 14, 'B', 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (20, 1, 18, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (21, 1, 19, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (22, 1, 7, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (23, 1, 10, 'C', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (24, 1, 9, 'B', 0, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_question_record` VALUES (25, 3, 19, 'B', 1, '2026-02-08 16:15:16', '2026-02-08 16:15:16', 0);

-- ----------------------------
-- Table structure for edu_wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `edu_wrong_question`;
CREATE TABLE `edu_wrong_question`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `question_id` bigint(0) NOT NULL,
  `wrong_count` int(0) NULL DEFAULT 1,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_wrong_question
-- ----------------------------
INSERT INTO `edu_wrong_question` VALUES (1, 1, 1, 2, '2026-02-06 09:59:45', '2026-02-06 09:59:45', 0);
INSERT INTO `edu_wrong_question` VALUES (2, 1, 2, 2, '2026-02-06 09:59:54', '2026-02-06 09:59:54', 0);
INSERT INTO `edu_wrong_question` VALUES (3, 1, 3, 2, '2026-02-06 09:59:57', '2026-02-06 09:59:57', 0);
INSERT INTO `edu_wrong_question` VALUES (4, 1, 4, 1, '2026-02-06 10:00:00', '2026-02-06 10:00:00', 0);
INSERT INTO `edu_wrong_question` VALUES (5, 1, 6, 1, '2026-02-06 14:17:24', '2026-02-06 14:17:25', 1);
INSERT INTO `edu_wrong_question` VALUES (6, 1, 5, 1, '2026-02-06 14:17:34', '2026-02-06 14:17:34', 0);
INSERT INTO `edu_wrong_question` VALUES (7, 3, 6, 2, '2026-02-08 11:57:10', '2026-02-08 11:57:10', 0);
INSERT INTO `edu_wrong_question` VALUES (8, 1, 16, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (9, 1, 12, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (10, 1, 13, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (11, 1, 18, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (12, 1, 19, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (13, 1, 7, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (14, 1, 10, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);
INSERT INTO `edu_wrong_question` VALUES (15, 1, 9, 1, '2026-02-08 15:29:55', '2026-02-08 15:29:55', 0);

-- ----------------------------
-- Table structure for oj_problem
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem`;
CREATE TABLE `oj_problem`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `difficulty` tinyint(0) NOT NULL DEFAULT 1,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `input_desc` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `output_desc` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `samples` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `time_limit_ms` int(0) NOT NULL DEFAULT 1000,
  `memory_limit_mb` int(0) NOT NULL DEFAULT 256,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for oj_submission
-- ----------------------------
DROP TABLE IF EXISTS `oj_submission`;
CREATE TABLE `oj_submission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `problem_id` bigint(0) NOT NULL,
  `homework_id` bigint(0) NULL DEFAULT NULL,
  `language` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `score` int(0) NOT NULL DEFAULT 0,
  `exec_time_ms` int(0) NULL DEFAULT NULL,
  `memory_mb` int(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sub_user`(`user_id`) USING BTREE,
  INDEX `idx_sub_problem`(`problem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SYSTEM',
  `status` tinyint(0) NULL DEFAULT 1,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '欢迎使用平台', '欢迎加入青少年编程平台，开始你的第一节课吧。', 'SYSTEM', 1, '2026-02-06 13:01:27', '2026-02-06 13:01:27', 0);
INSERT INTO `sys_notice` VALUES (2, '新课程上线', '算法入门课新增章节：排序与查找。', 'SYSTEM', 1, '2026-02-06 13:01:27', '2026-02-06 13:01:27', 0);
INSERT INTO `sys_notice` VALUES (3, '学习提醒', '今天已连续学习 3 天，保持节奏！', 'SYSTEM', 1, '2026-02-06 13:01:27', '2026-02-06 13:01:27', 0);
INSERT INTO `sys_notice` VALUES (4, '新的教师申请待审核', '用户：测试用户2（ID：5）申请成为教师，请尽快审核。', 'system', 1, '2026-02-06 15:29:35', '2026-02-06 15:29:35', 0);
INSERT INTO `sys_notice` VALUES (5, '教师申请审核通过', '用户：测试用户2（ID：5）教师申请已通过审核。 备注：奥德赛', 'system', 1, '2026-02-06 15:30:06', '2026-02-06 15:30:06', 0);
INSERT INTO `sys_notice` VALUES (6, '新的教师申请待审核', '用户：吕老师1（ID：8）申请成为教师，请尽快审核。', 'system', 1, '2026-02-06 15:35:26', '2026-02-06 15:35:26', 0);
INSERT INTO `sys_notice` VALUES (7, '教师申请审核通过', '用户：吕老师1（ID：8）教师申请已通过审核。 备注：大萨达', 'system', 1, '2026-02-06 15:36:02', '2026-02-06 15:36:02', 0);
INSERT INTO `sys_notice` VALUES (8, '新的教师申请待审核', '用户：测试一号（ID：9）申请成为教师，请尽快审核。', 'system', 1, '2026-02-06 15:42:58', '2026-02-06 15:42:58', 0);
INSERT INTO `sys_notice` VALUES (9, '教师申请审核通过', '用户：测试一号（ID：9）教师申请已通过审核。 备注：但是ASDA2', 'system', 1, '2026-02-06 15:43:30', '2026-02-06 15:43:30', 0);

-- ----------------------------
-- Table structure for sys_notice_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_user`;
CREATE TABLE `sys_notice_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `notice_id` bigint(0) NOT NULL,
  `is_read` tinyint(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`user_id`, `notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_user
-- ----------------------------
INSERT INTO `sys_notice_user` VALUES (1, 1, 2, 1, '2026-02-06 13:01:57', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (2, 1, 1, 1, '2026-02-06 13:02:01', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (3, 1, 3, 1, '2026-02-06 13:02:08', '2026-02-08 11:30:35', 1);
INSERT INTO `sys_notice_user` VALUES (4, 4, 2, 1, '2026-02-06 15:24:00', '2026-02-06 15:24:00', 0);
INSERT INTO `sys_notice_user` VALUES (5, 4, 3, 1, '2026-02-06 15:24:14', '2026-02-06 15:24:14', 0);
INSERT INTO `sys_notice_user` VALUES (6, 1, 4, 1, '2026-02-06 15:29:35', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (7, 5, 4, 1, '2026-02-06 15:29:40', '2026-02-06 15:29:40', 0);
INSERT INTO `sys_notice_user` VALUES (8, 5, 5, 0, '2026-02-06 15:30:06', '2026-02-06 15:30:06', 0);
INSERT INTO `sys_notice_user` VALUES (9, 1, 6, 0, '2026-02-06 15:35:26', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (10, 8, 1, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (11, 8, 2, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (12, 8, 3, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (13, 8, 4, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (14, 8, 5, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (15, 8, 6, 1, '2026-02-06 15:35:40', '2026-02-06 15:35:40', 0);
INSERT INTO `sys_notice_user` VALUES (17, 1, 5, 1, '2026-02-06 15:35:55', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (18, 8, 7, 0, '2026-02-06 15:36:02', '2026-02-06 15:36:02', 0);
INSERT INTO `sys_notice_user` VALUES (19, 1, 7, 1, '2026-02-06 15:40:01', '2026-02-06 15:40:01', 1);
INSERT INTO `sys_notice_user` VALUES (20, 3, 7, 1, '2026-02-06 15:40:32', '2026-02-06 15:40:32', 1);
INSERT INTO `sys_notice_user` VALUES (21, 3, 6, 1, '2026-02-06 15:40:32', '2026-02-06 15:40:32', 1);
INSERT INTO `sys_notice_user` VALUES (22, 3, 5, 1, '2026-02-06 15:40:33', '2026-02-06 15:40:33', 1);
INSERT INTO `sys_notice_user` VALUES (23, 3, 4, 1, '2026-02-06 15:40:34', '2026-02-06 15:40:34', 1);
INSERT INTO `sys_notice_user` VALUES (24, 3, 1, 1, '2026-02-06 15:40:34', '2026-02-06 15:40:34', 1);
INSERT INTO `sys_notice_user` VALUES (25, 3, 2, 1, '2026-02-06 15:40:35', '2026-02-06 15:40:35', 1);
INSERT INTO `sys_notice_user` VALUES (26, 3, 3, 1, '2026-02-06 15:40:35', '2026-02-06 15:40:35', 1);
INSERT INTO `sys_notice_user` VALUES (27, 1, 8, 0, '2026-02-06 15:42:58', '2026-02-08 11:30:34', 1);
INSERT INTO `sys_notice_user` VALUES (28, 9, 9, 1, '2026-02-06 15:43:30', '2026-02-06 15:43:30', 0);
INSERT INTO `sys_notice_user` VALUES (29, 9, 8, 1, '2026-02-06 15:44:16', '2026-02-06 15:44:16', 1);
INSERT INTO `sys_notice_user` VALUES (30, 9, 7, 1, '2026-02-06 15:44:16', '2026-02-06 15:44:16', 0);
INSERT INTO `sys_notice_user` VALUES (31, 9, 6, 1, '2026-02-06 15:44:18', '2026-02-06 15:44:18', 0);
INSERT INTO `sys_notice_user` VALUES (32, 9, 5, 1, '2026-02-06 15:56:43', '2026-02-06 15:56:43', 1);
INSERT INTO `sys_notice_user` VALUES (33, 9, 4, 1, '2026-02-06 15:56:44', '2026-02-06 15:56:44', 1);
INSERT INTO `sys_notice_user` VALUES (34, 9, 1, 1, '2026-02-06 15:56:44', '2026-02-06 15:56:44', 1);
INSERT INTO `sys_notice_user` VALUES (35, 9, 2, 1, '2026-02-06 15:56:44', '2026-02-06 15:56:44', 1);
INSERT INTO `sys_notice_user` VALUES (36, 9, 3, 1, '2026-02-06 15:56:44', '2026-02-06 15:56:44', 1);
INSERT INTO `sys_notice_user` VALUES (37, 1, 9, 1, '2026-02-08 10:13:42', '2026-02-08 10:13:42', 1);
INSERT INTO `sys_notice_user` VALUES (38, 3, 9, 1, '2026-02-08 11:31:55', '2026-02-08 11:31:55', 1);
INSERT INTO `sys_notice_user` VALUES (39, 3, 8, 1, '2026-02-08 11:31:55', '2026-02-08 11:31:55', 1);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'STUDENT', '学生', '2026-02-05 19:35:00', '2026-02-05 19:35:00', 0);
INSERT INTO `sys_role` VALUES (2, 'TEACHER', '教师', '2026-02-06 14:42:10', '2026-02-06 14:42:10', 0);
INSERT INTO `sys_role` VALUES (3, 'ADMIN', '管理员', '2026-02-06 14:48:51', '2026-02-06 14:48:51', 0);

-- ----------------------------
-- Table structure for sys_teacher_apply
-- ----------------------------
DROP TABLE IF EXISTS `sys_teacher_apply`;
CREATE TABLE `sys_teacher_apply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `status` tinyint(0) NULL DEFAULT 0,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `is_deleted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_teacher_apply
-- ----------------------------
INSERT INTO `sys_teacher_apply` VALUES (2, 3, 1, '很好，你现在可以当老师了', '2026-02-06 15:14:40', '2026-02-06 15:14:40', 0);
INSERT INTO `sys_teacher_apply` VALUES (3, 4, 1, '测试成功', '2026-02-06 15:22:55', '2026-02-06 15:22:55', 0);
INSERT INTO `sys_teacher_apply` VALUES (4, 5, 1, '奥德赛', '2026-02-06 15:29:35', '2026-02-06 15:29:35', 0);
INSERT INTO `sys_teacher_apply` VALUES (5, 8, 1, '大萨达', '2026-02-06 15:35:26', '2026-02-06 15:35:26', 0);
INSERT INTO `sys_teacher_apply` VALUES (6, 9, 1, '但是ASDA2', '2026-02-06 15:42:58', '2026-02-06 15:42:58', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(0) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'kagema12', '$2a$10$8lpkN0nqJ5oF.0rwoMZ9l.mbeASdlXsGvJ5qfSIZ5TExar4LVpBeq', '2522086087@qq.com', '19710119320', 'https://pic2.zhimg.com/v2-8d3f288feae0e511dee5c3d6735ca999_1440w.jpg', 1, '2026-02-05 19:35:00', '2026-02-05 19:35:00', 0);
INSERT INTO `sys_user` VALUES (3, '吕老师', '$2a$10$tVsPDcdUFQjuCm8MvVrPRuKhCxqX.lXOvCoyax116hxr0uykmD7ta', '19710119320@163.com', NULL, NULL, 1, '2026-02-06 15:14:40', '2026-02-06 15:14:40', 0);
INSERT INTO `sys_user` VALUES (4, '临时老师', '$2a$10$B4K02OPD.GsTGCA6EYGKFOioQcBn2S.H5KifMUG2GvWG0YdCbPJwS', '854riva@virgilian.com', NULL, NULL, 1, '2026-02-06 15:22:55', '2026-02-06 15:22:55', 0);
INSERT INTO `sys_user` VALUES (5, '测试用户2', '$2a$10$qCSa9ZUm/Im0sxm4vgEXJeYDqlKzzdoxoyCzHvnucW.mOKsfDOOUK', 'mlakd7uvnoew@ibymail.com', NULL, NULL, 1, '2026-02-06 15:29:35', '2026-02-06 15:29:35', 0);
INSERT INTO `sys_user` VALUES (8, '吕老师1', '$2a$10$ucIYfmCiUEofKjH6F3DKh..Ok4.ya1sGFoesb01NvXr7zU93jw8Zi', 'mlakjaacxlru@ibymail.com', NULL, NULL, 1, '2026-02-06 15:35:26', '2026-02-06 15:35:26', 0);
INSERT INTO `sys_user` VALUES (9, '测试一号', '$2a$10$48BMMdf69xW1/pg9cbUfbu.UvOt5o9Bs42tj21GrnXVQvc.gss7Jq', '3497986240@qq.com', NULL, NULL, 1, '2026-02-06 15:42:58', '2026-02-06 15:42:58', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `role_id` bigint(0) NOT NULL,
  `created_at` datetime(0) NOT NULL,
  `updated_at` datetime(0) NOT NULL,
  `is_deleted` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-02-05 19:35:00', '2026-02-05 19:35:00', 0);
INSERT INTO `sys_user_role` VALUES (2, 1, 3, '2026-02-06 14:51:26', '2026-02-06 14:51:26', 0);
INSERT INTO `sys_user_role` VALUES (4, 3, 1, '2026-02-06 15:14:40', '2026-02-06 15:14:40', 0);
INSERT INTO `sys_user_role` VALUES (5, 3, 2, '2026-02-06 15:15:12', '2026-02-06 15:15:12', 0);
INSERT INTO `sys_user_role` VALUES (6, 4, 1, '2026-02-06 15:22:55', '2026-02-06 15:22:55', 0);
INSERT INTO `sys_user_role` VALUES (7, 4, 2, '2026-02-06 15:23:19', '2026-02-06 15:23:19', 0);
INSERT INTO `sys_user_role` VALUES (8, 5, 1, '2026-02-06 15:29:35', '2026-02-06 15:29:35', 0);
INSERT INTO `sys_user_role` VALUES (9, 5, 2, '2026-02-06 15:30:06', '2026-02-06 15:30:06', 0);
INSERT INTO `sys_user_role` VALUES (10, 8, 1, '2026-02-06 15:35:26', '2026-02-06 15:35:26', 0);
INSERT INTO `sys_user_role` VALUES (11, 8, 2, '2026-02-06 15:36:02', '2026-02-06 15:36:02', 0);
INSERT INTO `sys_user_role` VALUES (12, 9, 1, '2026-02-06 15:42:58', '2026-02-06 15:42:58', 0);
INSERT INTO `sys_user_role` VALUES (13, 9, 2, '2026-02-06 15:43:30', '2026-02-06 15:43:30', 0);

SET FOREIGN_KEY_CHECKS = 1;
