from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
OUTPUT_DIR = ROOT / "thesis" / "figures"
CANVAS_WIDTH = 1600
CANVAS_HEIGHT = 900
BACKGROUND = "white"
BOX_FILL = "#F4F7FB"
BOX_OUTLINE = "#2F5597"
ACCENT = "#4F81BD"
TEXT = "#222222"
MUTED = "#666666"


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        "C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc",
        "C:/Windows/Fonts/simhei.ttf",
        "C:/Windows/Fonts/simsun.ttc",
    ]
    for candidate in candidates:
        path = Path(candidate)
        if path.exists():
            return ImageFont.truetype(str(path), size=size)
    return ImageFont.load_default()


TEXT_FONT = load_font(24)
SMALL_FONT = load_font(20)
ENTITY_TITLE_FONT = load_font(18, bold=True)
FIELD_FONT = load_font(13)
SECTION_FONT = load_font(22, bold=True)


def new_canvas(title: str, subtitle: str | None = None) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGB", (CANVAS_WIDTH, CANVAS_HEIGHT), BACKGROUND)
    return image, ImageDraw.Draw(image)


def new_sized_canvas(width: int, height: int) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGB", (width, height), BACKGROUND)
    return image, ImageDraw.Draw(image)


def draw_multiline_center(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
) -> None:
    lines = text.split("\n")
    line_metrics = [draw.textbbox((0, 0), line, font=font) for line in lines]
    heights = [bbox[3] - bbox[1] for bbox in line_metrics]
    total_height = sum(heights) + (len(lines) - 1) * 10
    current_y = xy[1] + ((xy[3] - xy[1]) - total_height) / 2
    for line, bbox, height in zip(lines, line_metrics, heights):
        width = bbox[2] - bbox[0]
        current_x = xy[0] + ((xy[2] - xy[0]) - width) / 2
        draw.text((current_x, current_y), line, fill=TEXT, font=font)
        current_y += height + 10


def draw_box(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    radius: int = 20,
    fill: str = BOX_FILL,
) -> None:
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=BOX_OUTLINE, width=3)
    draw_multiline_center(draw, xy, text, TEXT_FONT)


def draw_placeholder(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], text: str) -> None:
    draw.rounded_rectangle(xy, radius=24, fill="#FAFAFA", outline="#999999", width=3)
    draw_multiline_center(draw, xy, text, TEXT_FONT)


def truncate_text(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont, max_width: int) -> str:
    if draw.textbbox((0, 0), text, font=font)[2] <= max_width:
        return text
    ellipsis = "..."
    result = text
    while result:
        result = result[:-1]
        candidate = result + ellipsis
        if draw.textbbox((0, 0), candidate, font=font)[2] <= max_width:
            return candidate
    return ellipsis


def draw_arrow(
    draw: ImageDraw.ImageDraw,
    start: tuple[int, int],
    end: tuple[int, int],
    label: str | None = None,
) -> None:
    draw.line((*start, *end), fill=ACCENT, width=4)
    arrow_size = 12
    if end[0] == start[0]:
        direction = 1 if end[1] > start[1] else -1
        draw.polygon(
            [
                (end[0], end[1]),
                (end[0] - arrow_size, end[1] - direction * arrow_size),
                (end[0] + arrow_size, end[1] - direction * arrow_size),
            ],
            fill=ACCENT,
        )
    else:
        direction = 1 if end[0] > start[0] else -1
        draw.polygon(
            [
                (end[0], end[1]),
                (end[0] - direction * arrow_size, end[1] - arrow_size),
                (end[0] - direction * arrow_size, end[1] + arrow_size),
            ],
            fill=ACCENT,
        )
    if label:
        mid_x = (start[0] + end[0]) // 2
        mid_y = (start[1] + end[1]) // 2 - 28
        draw.text((mid_x - 30, mid_y), label, fill=MUTED, font=SMALL_FONT)


def draw_panel(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], title: str, fill: str) -> None:
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle(xy, radius=20, fill="#FCFDFF", outline=BOX_OUTLINE, width=3)
    draw.rounded_rectangle((x1 + 18, y1 + 14, x1 + 210, y1 + 54), radius=14, fill=fill, outline=BOX_OUTLINE, width=2)
    text_box = draw.textbbox((0, 0), title, font=SECTION_FONT)
    text_width = text_box[2] - text_box[0]
    text_height = text_box[3] - text_box[1]
    draw.text((x1 + 18 + (192 - text_width) / 2, y1 + 14 + (40 - text_height) / 2 - 1), title, fill=TEXT, font=SECTION_FONT)


def draw_entity(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    title: str,
    fields: list[str],
    header_fill: str,
) -> None:
    x1, y1, x2, y2 = xy
    header_height = 44
    draw.rounded_rectangle((x1 + 4, y1 + 4, x2 + 4, y2 + 4), radius=16, fill="#E8EEF8", outline="#E8EEF8", width=1)
    draw.rounded_rectangle(xy, radius=16, fill="#FFFFFF", outline=BOX_OUTLINE, width=3)
    draw.rounded_rectangle((x1, y1, x2, y1 + header_height), radius=16, fill=header_fill, outline=BOX_OUTLINE, width=3)
    draw.rectangle((x1 + 2, y1 + header_height - 18, x2 - 2, y1 + header_height + 2), fill=header_fill, outline=header_fill)

    title = truncate_text(draw, title, ENTITY_TITLE_FONT, x2 - x1 - 30)
    title_box = draw.textbbox((0, 0), title, font=ENTITY_TITLE_FONT)
    title_width = title_box[2] - title_box[0]
    title_height = title_box[3] - title_box[1]
    draw.text((x1 + ((x2 - x1) - title_width) / 2, y1 + (header_height - title_height) / 2 - 1), title, fill=TEXT, font=ENTITY_TITLE_FONT)

    row_height = (y2 - y1 - header_height) / max(len(fields), 1)
    current_y = y1 + header_height
    for index, field in enumerate(fields):
        if index > 0:
            draw.line((x1 + 12, current_y, x2 - 12, current_y), fill="#D7E0EF", width=2)
        badge_fill = "#E5E7EB"
        badge_text = None
        content = field
        if field.startswith("PK "):
            badge_fill = "#DBEAFE"
            badge_text = "PK"
            content = field[3:]
        elif field.startswith("FK "):
            badge_fill = "#DCFCE7"
            badge_text = "FK"
            content = field[3:]
        elif field.startswith("uk_"):
            badge_fill = "#F3E8FF"
            badge_text = "UK"
        text_x = x1 + 16
        if badge_text is not None:
            badge_box = (x1 + 16, current_y + 6, x1 + 52, current_y + 24)
            draw.rounded_rectangle(badge_box, radius=6, fill=badge_fill, outline="#D0D7E4", width=1)
            badge_text_box = draw.textbbox((0, 0), badge_text, font=FIELD_FONT)
            badge_text_width = badge_text_box[2] - badge_text_box[0]
            draw.text((badge_box[0] + (36 - badge_text_width) / 2, current_y + 7), badge_text, fill=TEXT, font=FIELD_FONT)
            text_x = x1 + 60
        content = truncate_text(draw, content, FIELD_FONT, x2 - text_x - 16)
        draw.text((text_x, current_y + 7), content, fill=TEXT, font=FIELD_FONT)
        current_y += row_height


def anchor(xy: tuple[int, int, int, int], side: str, ratio: float = 0.5) -> tuple[int, int]:
    x1, y1, x2, y2 = xy
    if side == "left":
        return x1, int(y1 + (y2 - y1) * ratio)
    if side == "right":
        return x2, int(y1 + (y2 - y1) * ratio)
    if side == "top":
        return int(x1 + (x2 - x1) * ratio), y1
    return int(x1 + (x2 - x1) * ratio), y2


def draw_relation(
    draw: ImageDraw.ImageDraw,
    points: list[tuple[int, int]],
    label: str,
    color: str = ACCENT,
) -> None:
    for start, end in zip(points, points[1:]):
        draw.line((*start, *end), fill=color, width=3)
    if not label:
        return

    def draw_one_marker(endpoint: tuple[int, int], neighbor: tuple[int, int]) -> None:
        dx = endpoint[0] - neighbor[0]
        dy = endpoint[1] - neighbor[1]
        if abs(dx) > abs(dy):
            offset = -8 if dx > 0 else 8
            x = endpoint[0] + offset
            draw.line((x, endpoint[1] - 10, x, endpoint[1] + 10), fill=color, width=3)
        else:
            offset = -8 if dy > 0 else 8
            y = endpoint[1] + offset
            draw.line((endpoint[0] - 10, y, endpoint[0] + 10, y), fill=color, width=3)

    def draw_many_marker(endpoint: tuple[int, int], neighbor: tuple[int, int]) -> None:
        dx = endpoint[0] - neighbor[0]
        dy = endpoint[1] - neighbor[1]
        if abs(dx) > abs(dy):
            outward = 1 if dx > 0 else -1
            base_x = endpoint[0] + 10 * outward
            draw.line((endpoint[0], endpoint[1], base_x, endpoint[1]), fill=color, width=3)
            draw.line((endpoint[0], endpoint[1], base_x, endpoint[1] - 10), fill=color, width=3)
            draw.line((endpoint[0], endpoint[1], base_x, endpoint[1] + 10), fill=color, width=3)
        else:
            outward = 1 if dy > 0 else -1
            base_y = endpoint[1] + 10 * outward
            draw.line((endpoint[0], endpoint[1], endpoint[0], base_y), fill=color, width=3)
            draw.line((endpoint[0], endpoint[1], endpoint[0] - 10, base_y), fill=color, width=3)
            draw.line((endpoint[0], endpoint[1], endpoint[0] + 10, base_y), fill=color, width=3)

    if label == "1:N":
        draw_one_marker(points[0], points[1])
        draw_many_marker(points[-1], points[-2])
    elif label == "N:1":
        draw_many_marker(points[0], points[1])
        draw_one_marker(points[-1], points[-2])
    elif label == "N:N":
        draw_many_marker(points[0], points[1])
        draw_many_marker(points[-1], points[-2])


def save(image: Image.Image, name: str) -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    background = Image.new(image.mode, image.size, BACKGROUND)
    diff = ImageChops.difference(image, background)
    bbox = diff.getbbox()
    if bbox is not None:
        padding = 24
        left = max(0, bbox[0] - padding)
        top = max(0, bbox[1] - padding)
        right = min(image.width, bbox[2] + padding)
        bottom = min(image.height, bbox[3] + padding)
        image = image.crop((left, top, right, bottom))
    image.save(OUTPUT_DIR / name, format="PNG")


def business_flow() -> None:
    image, draw = new_canvas("图3-1 系统业务流程图", "教师组织内容、学生参与学习、系统反馈结果、管理员进行治理")
    teacher = (80, 200, 380, 360)
    platform = (500, 185, 840, 375)
    student = (1040, 200, 1360, 360)
    records = (500, 470, 860, 650)
    feedback = (1040, 470, 1360, 650)
    admin = (80, 480, 380, 650)
    audit = (500, 730, 860, 860)

    draw_box(draw, teacher, "教师\n维护课程 / 题目\n考试 / 作业")
    draw_box(draw, platform, "青少年编程学习平台\n统一承载多角色\n教学业务")
    draw_box(draw, student, "学生\n学习课程 / 练习\n提交代码 / 参加考试")
    draw_box(draw, records, "学习行为数据\n选课记录 / 做题记录\n交卷记录 / 作业详情")
    draw_box(draw, feedback, "结果反馈\n通知 / 成长报告\n判题结果 / 成绩")
    draw_box(draw, admin, "管理员\n审核课程 / 教师申请\n社区治理")
    draw_box(draw, audit, "治理输出\n审核结果 / 审计日志")

    draw_arrow(draw, (380, 280), (500, 280))
    draw_arrow(draw, (840, 280), (1040, 280))
    draw_arrow(draw, (1200, 360), (1200, 470))
    draw_arrow(draw, (1040, 560), (860, 560))
    draw_arrow(draw, (500, 560), (380, 565))
    draw_arrow(draw, (230, 480), (230, 360))
    draw_arrow(draw, (680, 650), (680, 730))

    save(image, "system-business-flow.png")


def usecase_flow() -> None:
    image, draw = new_canvas("图3-2 角色用例图", "学生、教师、管理员围绕课程学习与平台治理开展协同")
    draw_box(draw, (70, 250, 270, 390), "学生")
    draw_box(draw, (70, 560, 270, 700), "管理员")
    draw_box(draw, (1330, 250, 1530, 390), "教师")

    usecases = [
        ((430, 120, 720, 230), "课程学习\n选课 / 课时浏览"),
        ((840, 120, 1130, 230), "客观题训练\n做题 / 收藏 / 错题回看"),
        ((430, 310, 720, 420), "编程题提交\n编辑代码 / 本地判题"),
        ((840, 310, 1130, 420), "考试与作业\n参加考试 / 查看作业"),
        ((430, 500, 720, 610), "课程与内容管理\n课程 / 章节 / 课时维护"),
        ((840, 500, 1130, 610), "题库与任务配置\n组卷 / 作业发布 / 统计"),
        ((635, 690, 925, 800), "平台审核与治理\n用户 / 课程 / 社区 / 教师申请"),
    ]
    for xy, text in usecases:
        draw_box(draw, xy, text)

    for target in [(430, 175), (430, 365), (840, 365), (840, 175)]:
        draw_arrow(draw, (270, 320), target)
    for target in [(1130, 555), (1130, 365), (1130, 175)]:
        draw_arrow(draw, (1330, 320), target)
    for target in [(635, 745), (430, 555), (840, 555)]:
        draw_arrow(draw, (270, 630), target)

    save(image, "usecase-flow.png")


def architecture() -> None:
    image, draw = new_canvas("图4-1 系统总体架构图", "前后端分离架构 + 安全控制 + 业务模块 + 数据存储")
    draw_box(draw, (90, 160, 540, 310), "浏览器 / 前端 SPA\nVue 3 + Vite\nElement Plus")
    draw_box(draw, (90, 355, 540, 505), "页面与路由层\n学生端 / 教师端 / 管理端")
    draw_box(draw, (90, 550, 540, 700), "Axios 网络层\nToken 注入 / 401 统一处理")

    draw_box(draw, (670, 160, 1090, 310), "Spring Boot REST API")
    draw_box(draw, (670, 355, 1090, 505), "Spring Security\nJWT Filter")
    draw_box(draw, (670, 550, 1090, 700), "Controller / Service / Mapper")

    draw_box(draw, (1200, 160, 1510, 310), "MySQL\n核心业务数据")
    draw_box(draw, (1200, 355, 1510, 505), "Redis\n缓存 / 临时状态")
    draw_box(draw, (1200, 560, 1510, 880), "核心业务模块\n认证与用户\n课程与学习\n题库与编程题\n考试与作业\n学习路径\n通知与社区治理")

    draw_arrow(draw, (315, 310), (315, 355))
    draw_arrow(draw, (315, 505), (315, 550))
    draw_arrow(draw, (540, 625), (670, 625), "HTTP")
    draw_arrow(draw, (880, 310), (880, 355))
    draw_arrow(draw, (880, 505), (880, 550))
    draw_arrow(draw, (1090, 235), (1200, 235))
    draw_arrow(draw, (1090, 430), (1200, 430))
    draw_arrow(draw, (1090, 625), (1200, 625))

    save(image, "system-architecture.png")


def function_structure() -> None:
    image, draw = new_canvas("图4-2 系统功能结构图", "统一入口下的学生端、教师端、管理端与公共支撑模块")
    draw_box(draw, (430, 60, 1170, 180), "统一入口与认证层\n邮箱登录 / 注册 / JWT 鉴权 / 角色分流")

    draw_panel(draw, (70, 240, 470, 760), "学生端", "#DBEAFE")
    draw_panel(draw, (600, 240, 1000, 760), "教师端", "#DCFCE7")
    draw_panel(draw, (1130, 240, 1530, 760), "管理端", "#FCE7F3")

    blocks = [
        ((110, 305, 430, 390), "课程学习\n课程 / 章节 / 课时"),
        ((110, 415, 430, 500), "训练中心\n客观题 / 错题 / 收藏"),
        ((110, 525, 430, 610), "编程实战\n代码编辑 / 本地判题"),
        ((110, 635, 430, 720), "任务反馈\n考试 / 作业 / 报告 / AI 助手"),
        ((640, 305, 960, 390), "课程维护\n课程 / 章节 / 课时管理"),
        ((640, 415, 960, 500), "题库工作台\n客观题 / 编程题维护"),
        ((640, 525, 960, 610), "教学任务\n考试组卷 / 作业发布"),
        ((640, 635, 960, 720), "教学统计\n作业统计 / 学习分析"),
        ((1170, 305, 1490, 390), "用户治理\n用户状态 / 角色审核"),
        ((1170, 415, 1490, 500), "课程审核\n课程上下架 / 教师申请"),
        ((1170, 525, 1490, 610), "社区治理\n帖子 / 回复审核"),
        ((1170, 635, 1490, 720), "平台支撑\n通知 / 审计日志 / 运营查看"),
    ]
    for xy, text in blocks:
        draw_box(draw, xy, text, radius=16)

    draw_box(draw, (260, 800, 1340, 880), "公共支撑层\n统一响应 / 权限控制 / 缓存存储 / 文件资源 / 通知服务 / AI 学习助手")
    draw_arrow(draw, (560, 180), (270, 240))
    draw_arrow(draw, (800, 180), (800, 240))
    draw_arrow(draw, (1040, 180), (1330, 240))

    save(image, "system-function-structure.png")


def er_diagram() -> None:
    image, draw = new_canvas("图4-3 系统总体 E-R 概览图", "以核心实体与主关系链展示系统总体概念模型")
    entities = {
        "sys_role": ((80, 110, 380, 255), "sys_role（角色）", ["PK id", "code", "name", "created_at"], "#DBEAFE"),
        "sys_user": ((80, 330, 380, 500), "sys_user（用户）", ["PK id", "username / email", "status / mute_status", "created_at"], "#DBEAFE"),
        "edu_course": ((650, 110, 970, 280), "edu_course（课程）", ["PK id", "FK teacher_id", "title / intro", "status / finish_status"], "#DCFCE7"),
        "edu_learning_unit": ((650, 360, 970, 530), "章节 / 课时", ["FK course_id", "title", "content_type", "sort_no"], "#DCFCE7"),
        "edu_question": ((1180, 100, 1500, 245), "edu_question（客观题）", ["PK id", "FK course_id", "type / difficulty", "status"], "#EDE9FE"),
        "edu_code_problem": ((1180, 285, 1500, 430), "edu_code_problem（编程题）", ["PK id", "FK course_id", "difficulty", "time_limit / status"], "#EDE9FE"),
        "edu_exam_homework": ((1180, 470, 1500, 640), "考试 / 作业任务", ["PK id", "FK course_id", "title", "time_window / status"], "#FEF3C7"),
        "edu_learning_trace": ((650, 650, 970, 830), "学习记录 / 提交记录", ["FK user_id", "FK course_id / problem_id", "result / score", "created_at"], "#CCFBF1"),
    }
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for key, (xy, title, fields, fill) in entities.items():
        boxes[key] = xy
        draw_entity(draw, xy, title, fields, fill)

    relation_color = "#5B7DB8"
    draw_relation(draw, [anchor(boxes["sys_role"], "bottom", 0.5), anchor(boxes["sys_user"], "top", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.5), anchor(boxes["edu_course"], "left", 0.35)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_course"], "bottom", 0.5), anchor(boxes["edu_learning_unit"], "top", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_course"], "right", 0.35), anchor(boxes["edu_question"], "left", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_course"], "right", 0.65), anchor(boxes["edu_code_problem"], "left", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_course"], "right", 0.9), anchor(boxes["edu_exam_homework"], "left", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.8), (520, 765), anchor(boxes["edu_learning_trace"], "left", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_learning_unit"], "bottom", 0.5), anchor(boxes["edu_learning_trace"], "top", 0.3)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_question"], "bottom", 0.5), anchor(boxes["edu_learning_trace"], "right", 0.2)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_code_problem"], "bottom", 0.5), anchor(boxes["edu_learning_trace"], "right", 0.5)], "1:N", relation_color)
    draw_relation(draw, [anchor(boxes["edu_exam_homework"], "bottom", 0.5), anchor(boxes["edu_learning_trace"], "right", 0.8)], "1:N", relation_color)

    save(image, "system-er.png")


def user_course_er() -> None:
    image, draw = new_sized_canvas(1500, 980)
    entities = {
        "sys_user": ((60, 120, 420, 280), "sys_user（用户）", ["PK id", "username", "email", "status", "mute_status"], "#DBEAFE"),
        "sys_role": ((520, 120, 880, 280), "sys_role（角色）", ["PK id", "code", "name", "created_at", "updated_at"], "#DBEAFE"),
        "sys_user_role": ((290, 340, 650, 500), "sys_user_role（用户角色）", ["PK id", "FK user_id", "FK role_id", "uk_user_role", "created_at"], "#DBEAFE"),
        "edu_course": ((1050, 120, 1410, 280), "edu_course（课程）", ["PK id", "FK teacher_id", "title", "status", "finish_status"], "#DCFCE7"),
        "edu_course_enroll": ((1050, 340, 1410, 500), "edu_course_enroll（选课）", ["PK id", "FK user_id", "FK course_id", "progress", "last_lesson_id"], "#DCFCE7"),
        "edu_chapter": ((1050, 560, 1410, 720), "edu_chapter（章节）", ["PK id", "FK course_id", "title", "sort_no", "updated_at"], "#DCFCE7"),
        "edu_lesson": ((1050, 780, 1410, 940), "edu_lesson（课时）", ["PK id", "FK chapter_id", "title", "content_type", "sort_no"], "#DCFCE7"),
        "edu_learn_record": ((520, 600, 880, 760), "edu_learn_record（学习记录）", ["PK id", "FK user_id", "FK lesson_id", "progress", "is_finished"], "#CCFBF1"),
    }
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for key, (xy, title, fields, fill) in entities.items():
        boxes[key] = xy
        draw_entity(draw, xy, title, fields, fill)

    color = "#5B7DB8"
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.5), anchor(boxes["sys_user_role"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_role"], "left", 0.5), anchor(boxes["sys_user_role"], "right", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.8), (950, 240), anchor(boxes["edu_course"], "left", 0.35)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_course"], "bottom", 0.5), anchor(boxes["edu_course_enroll"], "top", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.2), (930, 180), anchor(boxes["edu_course_enroll"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_course"], "bottom", 0.7), anchor(boxes["edu_chapter"], "top", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_chapter"], "bottom", 0.5), anchor(boxes["edu_lesson"], "top", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_lesson"], "left", 0.5), anchor(boxes["edu_learn_record"], "right", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_user"], "bottom", 0.5), anchor(boxes["edu_learn_record"], "left", 0.4)], "1:N", color)

    save(image, "user-course-er.png")


def question_knowledge_er() -> None:
    image, draw = new_sized_canvas(1520, 980)
    entities = {
        "edu_question": ((60, 120, 420, 280), "edu_question（客观题）", ["PK id", "FK course_id", "FK chapter_id", "type / difficulty", "status"], "#EDE9FE"),
        "edu_question_option": ((500, 120, 860, 280), "edu_question_option（题目选项）", ["PK id", "FK question_id", "label", "content", "is_correct"], "#EDE9FE"),
        "edu_question_record": ((940, 120, 1300, 280), "edu_question_record（做题记录）", ["PK id", "FK user_id", "FK question_id", "is_correct", "score"], "#EDE9FE"),
        "edu_wrong_question": ((60, 360, 420, 520), "edu_wrong_question（错题本）", ["PK id", "FK user_id", "FK question_id", "wrong_count", "mastered_at"], "#EDE9FE"),
        "edu_question_favorite": ((500, 360, 860, 520), "edu_question_favorite（题目收藏）", ["PK id", "FK user_id", "FK question_id", "created_at", "updated_at"], "#EDE9FE"),
        "edu_knowledge_point": ((940, 360, 1300, 520), "edu_knowledge_point（知识点）", ["PK id", "FK course_id", "title", "difficulty", "status"], "#EDE9FE"),
        "edu_knowledge_progress": ((500, 610, 860, 770), "edu_knowledge_progress（知识进度）", ["PK id", "FK user_id", "FK point_id", "status", "score"], "#CCFBF1"),
        "edu_knowledge_dependency": ((940, 610, 1300, 770), "edu_knowledge_dependency（知识依赖）", ["PK id", "FK point_id", "FK prerequisite_id", "relation_type", "updated_at"], "#CCFBF1"),
    }
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for key, (xy, title, fields, fill) in entities.items():
        boxes[key] = xy
        draw_entity(draw, xy, title, fields, fill)

    color = "#5B7DB8"
    draw_relation(draw, [anchor(boxes["edu_question"], "right", 0.5), anchor(boxes["edu_question_option"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_question"], "right", 0.8), anchor(boxes["edu_question_record"], "left", 0.2)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_question"], "bottom", 0.3), anchor(boxes["edu_wrong_question"], "top", 0.3)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_question"], "bottom", 0.7), anchor(boxes["edu_question_favorite"], "top", 0.7)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_knowledge_point"], "bottom", 0.3), anchor(boxes["edu_knowledge_progress"], "top", 0.7)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_knowledge_point"], "bottom", 0.7), anchor(boxes["edu_knowledge_dependency"], "top", 0.3)], "1:N", color)

    save(image, "question-knowledge-er.png")


def exam_homework_er() -> None:
    image, draw = new_sized_canvas(1520, 980)
    entities = {
        "sys_user": ((60, 120, 420, 280), "sys_user（用户）", ["PK id", "username", "email", "status", "mute_status"], "#DBEAFE"),
        "edu_course": ((500, 120, 860, 280), "edu_course（课程）", ["PK id", "FK teacher_id", "title", "status", "finish_status"], "#DCFCE7"),
        "edu_exam_task": ((940, 120, 1300, 280), "edu_exam_task（考试任务）", ["PK id", "FK course_id", "title", "duration_minutes", "status"], "#FEF3C7"),
        "edu_exam_task_question": ((940, 360, 1300, 520), "edu_exam_task_question（考试题目）", ["PK id", "FK task_id", "question_id", "score", "sort_no"], "#FEF3C7"),
        "edu_exam_submission": ((500, 360, 860, 520), "edu_exam_submission（考试提交）", ["PK id", "FK task_id", "FK user_id", "score", "submitted_at"], "#FEF3C7"),
        "edu_homework": ((60, 610, 420, 770), "edu_homework（作业）", ["PK id", "FK course_id", "FK teacher_id", "title", "deadline"], "#FEF3C7"),
        "edu_homework_problem": ((500, 610, 860, 770), "edu_homework_problem（作业题目）", ["PK id", "FK homework_id", "FK problem_id", "score", "uk_hw_problem"], "#FEF3C7"),
    }
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for key, (xy, title, fields, fill) in entities.items():
        boxes[key] = xy
        draw_entity(draw, xy, title, fields, fill)

    color = "#5B7DB8"
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.7), anchor(boxes["edu_course"], "left", 0.35)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_course"], "right", 0.5), anchor(boxes["edu_exam_task"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_exam_task"], "bottom", 0.5), anchor(boxes["edu_exam_task_question"], "top", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_exam_task"], "left", 0.75), anchor(boxes["edu_exam_submission"], "right", 0.2)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.35), anchor(boxes["edu_exam_submission"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_course"], "left", 0.75), (250, 520), anchor(boxes["edu_homework"], "top", 0.6)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_homework"], "right", 0.5), anchor(boxes["edu_homework_problem"], "left", 0.5)], "1:N", color)

    save(image, "exam-homework-er.png")


def code_eval_er() -> None:
    image, draw = new_sized_canvas(1500, 920)
    entities = {
        "sys_user": ((60, 120, 420, 280), "sys_user（用户）", ["PK id", "username", "email", "status", "mute_status"], "#DBEAFE"),
        "edu_course": ((520, 120, 880, 280), "edu_course（课程）", ["PK id", "FK teacher_id", "title", "status", "finish_status"], "#DCFCE7"),
        "edu_code_problem": ((1020, 120, 1380, 280), "edu_code_problem（编程题）", ["PK id", "FK teacher_id", "FK course_id", "difficulty", "time_limit"], "#CCFBF1"),
        "edu_code_testcase": ((1020, 380, 1380, 540), "edu_code_testcase（测试用例）", ["PK id", "FK problem_id", "input_data", "output_data", "is_sample"], "#CCFBF1"),
        "edu_code_submission": ((520, 620, 880, 780), "edu_code_submission（代码提交）", ["PK id", "FK user_id", "FK problem_id", "language_id / result", "passed_count"], "#CCFBF1"),
    }
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for key, (xy, title, fields, fill) in entities.items():
        boxes[key] = xy
        draw_entity(draw, xy, title, fields, fill)

    color = "#5B7DB8"
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.5), anchor(boxes["edu_code_submission"], "left", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_course"], "right", 0.5), anchor(boxes["edu_code_problem"], "left", 0.35)], "1:N", color)
    draw_relation(draw, [anchor(boxes["sys_user"], "right", 0.25), (950, 160), anchor(boxes["edu_code_problem"], "left", 0.7)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_code_problem"], "bottom", 0.5), anchor(boxes["edu_code_testcase"], "top", 0.5)], "1:N", color)
    draw_relation(draw, [anchor(boxes["edu_code_problem"], "left", 0.75), anchor(boxes["edu_code_submission"], "right", 0.3)], "1:N", color)

    save(image, "code-eval-er.png")


def auth_role_flow() -> None:
    image, draw = new_canvas("图4-8 登录与角色分流流程图", "统一登录入口下的认证、鉴权与多角色门户分发过程")
    steps = [
        ("登录页提交\n邮箱与密码", 150),
        ("认证接口校验\n邮箱 / 密码 / 状态", 270),
        ("生成 JWT\n返回用户与角色信息", 390),
        ("前端保存 Token\n写入用户上下文", 510),
        ("路由守卫校验\nmeta.roles 与登录态", 630),
    ]
    for text, center_y in steps:
        draw_box(draw, (450, center_y - 45, 1150, center_y + 45), text)
    for index in range(len(steps) - 1):
        draw_arrow(draw, (800, steps[index][1] + 45), (800, steps[index + 1][1] - 45))

    draw_box(draw, (120, 740, 430, 860), "学生门户\n/dashboard / courses / practice")
    draw_box(draw, (645, 740, 955, 860), "教师门户\n/teacher / teacher/questions")
    draw_box(draw, (1170, 740, 1480, 860), "管理门户\n/admin / admin/users")
    draw_arrow(draw, (800, 675), (275, 740))
    draw_arrow(draw, (800, 675), (800, 740))
    draw_arrow(draw, (800, 675), (1325, 740))

    draw_box(draw, (1220, 120, 1480, 300), "受保护接口访问\nJwtAuthFilter 解析 Token\n恢复 SecurityContext\n@PreAuthorize 二次校验")
    draw_arrow(draw, (1150, 270), (1220, 210))

    save(image, "auth-role-flow.png")


def judge_flow() -> None:
    image, draw = new_sized_canvas(1600, 1040)
    steps = [
        ("接收代码提交", 150),
        ("校验题目 / 语言\n与用户身份", 270),
        ("创建临时工作目录", 390),
        ("按语言选择编译器\n或解释执行器", 510),
        ("逐个执行测试用例", 630),
        ("比对输出并汇总结果", 750),
        ("保存提交记录\n并返回结果", 890),
    ]
    for text, center_y in steps:
        draw_box(draw, (430, center_y - 42, 1170, center_y + 42), text)
    for index in range(len(steps) - 1):
        draw_arrow(draw, (800, steps[index][1] + 42), (800, steps[index + 1][1] - 42))

    draw_box(draw, (90, 470, 330, 600), "C / C++\n先编译后运行")
    draw_box(draw, (1270, 470, 1510, 600), "Python\n直接解释执行")
    draw_arrow(draw, (430, 510), (330, 535))
    draw_arrow(draw, (1170, 510), (1270, 535))

    save(image, "judge-flow.png")


def homework_exam_flow() -> None:
    image, draw = new_canvas("图4-10 作业考试闭环流程图", "从任务发布、学生参与到成绩反馈和统计分析的闭环处理过程")
    draw_box(draw, (80, 240, 350, 380), "教师发布任务\n作业 / 考试配置")
    draw_box(draw, (430, 240, 760, 380), "系统生成任务\n关联课程 / 题目 / 时间窗口")
    draw_box(draw, (840, 240, 1130, 380), "学生接收任务\n作答 / 提交 / 交卷")
    draw_box(draw, (1210, 240, 1510, 380), "自动评分与记录\n成绩 / 判题结果 / 提交详情")

    draw_box(draw, (840, 520, 1130, 660), "结果反馈\n通知 / 成长报告 / 历史记录")
    draw_box(draw, (430, 520, 760, 660), "教师查看统计\n作业完成率 / 成绩分布")
    draw_box(draw, (80, 520, 350, 660), "管理员审计\n课程 / 内容 / 行为治理")

    draw_arrow(draw, (350, 310), (430, 310))
    draw_arrow(draw, (760, 310), (840, 310))
    draw_arrow(draw, (1130, 310), (1210, 310))
    draw_arrow(draw, (1360, 380), (980, 520))
    draw_arrow(draw, (840, 590), (760, 590))
    draw_arrow(draw, (430, 590), (350, 590))
    draw_arrow(draw, (220, 520), (220, 380))

    save(image, "homework-exam-flow.png")


def screenshot_placeholder(name: str, title: str) -> None:
    image, draw = new_canvas(title, "待系统启动后补充真实运行截图")
    draw_placeholder(draw, (140, 200, 1460, 780), "此处插入系统运行截图\n建议分辨率：1440 × 900 以上\n可展示导航、表格、表单或关键交互")
    save(image, name)


def main() -> None:
    business_flow()
    usecase_flow()
    architecture()
    function_structure()
    er_diagram()
    user_course_er()
    question_knowledge_er()
    exam_homework_er()
    code_eval_er()
    auth_role_flow()
    judge_flow()
    homework_exam_flow()
    screenshot_placeholder("student-pages-placeholder.png", "图5-1 学生端关键页面截图")
    screenshot_placeholder("teacher-pages-placeholder.png", "图5-2 教师端关键页面截图")
    screenshot_placeholder("admin-pages-placeholder.png", "图5-3 管理端关键页面截图")


if __name__ == "__main__":
    main()
