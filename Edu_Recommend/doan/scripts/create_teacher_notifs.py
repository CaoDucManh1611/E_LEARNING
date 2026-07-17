import os
import re

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

list_html = """<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thông Báo Hệ Thống — EduRecommend</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --bg:       #faf7f2;
            --surface:  #ffffff;
            --border:   #e4ddd3;
            --green:    #2d6a4f;
            --green2:   #40916c;
            --green-lt: #d8f3dc;
            --text:     #1c2b22;
            --muted:    #7a8c80;
            --accent:   #b7791f;
            --shadow:   0 2px 16px rgba(45,106,79,.06);
        }
        body { background: var(--bg); color: var(--text); font-family: 'Inter', sans-serif; min-height: 100vh; display: flex; }
        
        .sidebar { width: 260px; background: var(--surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; padding: 30px 20px; position: fixed; top: 0; bottom: 0; left: 0; z-index: 100; }
        .logo { font-weight: 800; font-size: 1.3rem; letter-spacing: -.5px; color: var(--green); margin-bottom: 40px; text-align: center; }
        .logo em { font-style: normal; color: var(--accent); }
        .menu-title { font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: var(--muted); letter-spacing: 1px; margin-bottom: 12px; padding-left: 8px; }
        .nav-links { display: flex; flex-direction: column; gap: 6px; flex-grow: 1; }
        .nav-link { display: flex; align-items: center; justify-content: space-between; color: var(--text); text-decoration: none; font-size: 0.9rem; font-weight: 500; padding: 12px 16px; border-radius: 10px; transition: all .18s; }
        .nav-link:hover { background: var(--green-lt); color: var(--green); }
        .nav-link.active { background: var(--green); color: white; font-weight: 600; }
        
        .main-content { margin-left: 260px; flex: 1; padding: 40px 48px; min-height: 100vh; }
        .page-header { margin-bottom: 30px; }
        .page-title { font-size: 1.6rem; font-weight: 800; letter-spacing: -.5px; color: var(--text); margin-bottom: 6px; }
        .page-sub { font-size: 0.88rem; color: var(--muted); }
        
        .notif-list { display: flex; flex-direction: column; gap: 12px; max-width: 800px; }
        .notif-card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px; text-decoration: none; color: inherit; display: block; transition: all 0.2s; position: relative; overflow: hidden; }
        .notif-card:hover { transform: translateY(-2px); box-shadow: var(--shadow); border-color: var(--green-lt); }
        .notif-card.unread { background: #fffdf7; border-left: 4px solid var(--accent); }
        .notif-title { font-size: 1.05rem; font-weight: 700; color: var(--green); margin-bottom: 8px; }
        .notif-body { font-size: 0.9rem; color: var(--text); line-height: 1.5; margin-bottom: 12px; }
        .notif-time { font-size: 0.75rem; color: var(--muted); }
        .notif-sender { display: inline-flex; align-items: center; gap: 6px; background: var(--bg); padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; border: 1px solid var(--border); margin-bottom: 8px;}
        
        .badge { background: #dc2626; color: white; font-size: 0.75rem; font-weight: 800; padding: 2px 8px; border-radius: 12px; }
    </style>
</head>
<body>

<div class="sidebar">
    <div class="logo">Edu<em>Recommend</em></div>
    <div class="menu-title">Điều hướng</div>
    <div class="nav-links">
        <a href="/teacher/dashboard" class="nav-link">
            <div style="display:flex; align-items:center; gap:12px;"><span>📊</span> Tổng quan</div>
        </a>
        <a href="/teacher/courses" class="nav-link">
            <div style="display:flex; align-items:center; gap:12px;"><span>📚</span> Khóa học của tôi</div>
        </a>
        <a href="/teacher/notifications" class="nav-link active">
            <div style="display:flex; align-items:center; gap:12px;"><span>🔔</span> Thông báo</div>
            <span class="badge" th:if="${globalUnreadCount > 0}" th:text="${globalUnreadCount}">2</span>
        </a>
        
        <div class="menu-title" style="margin-top: 20px;">Hệ thống</div>
        <a href="/" class="nav-link"><div style="display:flex; align-items:center; gap:12px;"><span>🏠</span> Về Trang chủ</div></a>
        <a href="/logout" class="nav-link" style="color: #dc2626;"><div style="display:flex; align-items:center; gap:12px;"><span>🚪</span> Đăng xuất</div></a>
    </div>
</div>

<div class="main-content">
    <div class="page-header">
        <h1 class="page-title">Thông Báo Hệ Thống</h1>
        <p class="page-sub">Xem phản hồi, bình luận và đánh giá từ học viên.</p>
    </div>

    <div class="notif-list">
        <div th:if="${#lists.isEmpty(notifications)}" style="text-align: center; color: var(--muted); padding: 40px; background: var(--surface); border-radius: 16px; border: 1px dashed var(--border);">
            Bạn không có thông báo nào.
        </div>
        
        <a th:each="n : ${notifications}" 
           th:href="@{/teacher/notifications/{id}/click(id=${n.id})}" 
           class="notif-card" th:classappend="${!n.daDoc} ? 'unread' : ''">
           
            <div class="notif-sender" th:if="${n.sender != null}">
                👤 <span th:text="${n.sender.hoTen}">Tên học viên</span>
            </div>
            <h3 class="notif-title" th:text="${n.tieuDe}">Tiêu đề thông báo</h3>
            <div class="notif-body" th:text="${n.noiDung}">Nội dung chi tiết thông báo...</div>
            <div class="notif-time" th:text="${n.createdAt != null ? #temporals.format(n.createdAt, 'dd/MM/yyyy HH:mm') : 'Vừa xong'}">16/07/2026 10:00</div>
        </a>
    </div>
</div>

</body>
</html>
"""
write_file(r'd:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\notifications\list.html', list_html)

# Add sidebar link to course-list.html
path1 = r'd:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\course-list.html'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

sidebar_link = """        <a href="/teacher/courses" class="nav-link active">
            <span>📚</span> Khóa học của tôi
        </a>
        <a href="/teacher/notifications" class="nav-link" style="display:flex; justify-content:space-between; align-items:center;">
            <div><span>🔔</span> Thông báo</div>
            <span th:if="${globalUnreadCount > 0}" th:text="${globalUnreadCount}" style="background:#dc2626; color:white; font-size:0.75rem; font-weight:800; padding:2px 8px; border-radius:12px;">2</span>
        </a>"""
content1 = content1.replace("""        <a href="/teacher/courses" class="nav-link active">
            <span>📚</span> Khóa học của tôi
        </a>""", sidebar_link)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Done creating notifications")
