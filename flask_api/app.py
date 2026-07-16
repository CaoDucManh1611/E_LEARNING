# ============================================================
# Flask API: KNN + Apriori Course Recommendation System
# Chuyển đổi từ Google Colab Notebook → Standalone Flask App
# Deploy lên Render / Railway / bất kỳ cloud nào
# ============================================================

import os, ast, warnings, io, base64, json, sys
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns

# Fix encoding on Windows
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

from flask import Flask, request, jsonify
from flask_cors import CORS
from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import KNeighborsClassifier
from mlxtend.preprocessing import TransactionEncoder
from mlxtend.frequent_patterns import apriori, association_rules

warnings.filterwarnings('ignore')

app = Flask(__name__)
CORS(app)  # Cho phép Spring Boot gọi từ domain khác

# ── ĐỌC DỮ LIỆU ──────────────────────────────────────────────
BASE_DIR   = os.path.dirname(os.path.abspath(__file__))
df_student = pd.read_csv(os.path.join(BASE_DIR, 'students_cleaned_new.csv'))
df_course  = pd.read_csv(os.path.join(BASE_DIR, 'courses_cleaned.csv'))

print(f"[INFO] Sinh vien: {len(df_student)} dong")
print(f"[INFO] Khoa hoc : {len(df_course)} dong")

# ── 17 FEATURE COLS (thứ tự PHẢI khớp với StudentRequest.java) ─
FEATURE_COLS = [
    'Hours_Studied', 'Attendance', 'Previous_Scores', 'Sleep_Hours',
    'Tutoring_Sessions', 'Physical_Activity',
    'Parental_Involvement', 'Access_to_Resources', 'Extracurricular_Activities',
    'Motivation_Level', 'Internet_Access', 'Family_Income',
    'Peer_Influence', 'Learning_Disabilities', 'Parental_Education_Level',
    'Distance_from_Home', 'Gender',
]

DIFFICULTY_MAP = {
    0: ['Beginner'],
    1: ['Beginner', 'Mixed'],
    2: ['Mixed', 'Intermediate'],
    3: ['Intermediate', 'Advanced']
}
GROUP_LABEL = {0: 'Yếu', 1: 'Trung bình', 2: 'Khá', 3: 'Giỏi'}

SEED_SKILLS = {
    0: ['Communication', 'Project Management', 'Microsoft Excel'],
    1: ['SQL', 'Data Analysis', 'Microsoft Excel'],
    2: ['Python Programming', 'Data Science', 'Data Analysis'],
    3: ['Machine Learning', 'Deep Learning', 'Data Science']
}

# ── TRAIN KNN ─────────────────────────────────────────────────
y     = df_student['GROUP'].values
X_raw = df_student[FEATURE_COLS].values.copy().astype(float)

# Tăng trọng số 3 feature quan trọng nhất x3 (giống lúc train)
X_raw[:, 0] *= 3   # Hours_Studied
X_raw[:, 1] *= 3   # Attendance
X_raw[:, 2] *= 3   # Previous_Scores

scaler    = StandardScaler()
X_scaled  = scaler.fit_transform(X_raw)

knn_model = KNeighborsClassifier(n_neighbors=7, metric='euclidean')
knn_model.fit(X_scaled, y)
print("[INFO] KNN trained OK (n_neighbors=7, weighted x3)")

# ── APRIORI ───────────────────────────────────────────────────
df_course['skills_parsed'] = df_course['course_skills'].apply(ast.literal_eval)

te       = TransactionEncoder()
te_array = te.fit_transform(df_course['skills_parsed'].tolist())
df_te    = pd.DataFrame(te_array, columns=te.columns_)

frequent_itemsets = apriori(df_te, min_support=0.01, use_colnames=True)
rules = association_rules(frequent_itemsets, metric='confidence', min_threshold=0.20)
rules = rules[rules['lift'] >= 2.0].sort_values('lift', ascending=False).reset_index(drop=True)
print(f"[INFO] Apriori: {len(rules)} rules (lift >= 2.0)")


def get_apriori_skills(input_skills: list, top_n=15) -> list:
    """Mở rộng kỹ năng bằng Apriori association rules."""
    related = {}
    input_set = set(input_skills)
    for _, row in rules.iterrows():
        if set(row['antecedents']).issubset(input_set):
            for sk in row['consequents']:
                related[sk] = max(related.get(sk, 0), float(row['lift']))
    for sk in input_skills:
        if sk not in related:
            related[sk] = 5.0
    return [s for s, _ in sorted(related.items(), key=lambda x: x[1], reverse=True)][:top_n]


def recommend(encoded_vector: list, input_skills: list = None, top_n: int = 5):
    """
    Hàm gợi ý chính:
    1. KNN → dự đoán nhóm học lực (0=Yếu, 1=Trung bình, 2=Khá, 3=Giỏi)
    2. Apriori → mở rộng kỹ năng liên quan
    3. Lọc + xếp hạng khóa học phù hợp
    """
    # GIAI ĐOẠN 1: KNN
    vec = np.array(encoded_vector, dtype=float)
    vec[0] *= 3   # Hours_Studied
    vec[1] *= 3   # Attendance
    vec[2] *= 3   # Previous_Scores
    x_scaled   = scaler.transform(vec.reshape(1, -1))
    group_pred = int(knn_model.predict(x_scaled)[0])
    difficulties = DIFFICULTY_MAP[group_pred]

    # GIAI ĐOẠN 2: Kỹ năng
    if not input_skills:
        input_skills = SEED_SKILLS[group_pred]
    expanded_skills = get_apriori_skills(input_skills, top_n=20)

    # GIAI ĐOẠN 3: Lọc + xếp hạng khóa học
    df_filtered = df_course[df_course['course_difficulty'].isin(difficulties)].copy()
    skill_set   = set(expanded_skills)
    df_filtered['skill_match'] = df_filtered['skills_parsed'].apply(
        lambda x: len(set(x) & skill_set)
    )
    top_courses = (df_filtered
                   .sort_values(by=['skill_match', 'course_rating'], ascending=[False, False])
                   .head(top_n))

    # GIAI ĐOẠN 4: Đóng gói kết quả
    courses_out = []
    for _, row in top_courses.iterrows():
        matched = list(set(row['skills_parsed']) & skill_set)[:3]
        courses_out.append({
            'ten_khoa_hoc'  : str(row['course_title']),
            'to_chuc'       : str(row['course_organization']),
            'cap_do'        : str(row['course_difficulty']),
            'diem_danh_gia' : float(row['course_rating']) if pd.notna(row['course_rating']) else 0.0,
            'loai_chung_chi': str(row['course_certificate_type']),
            'duong_dan'     : str(row['course_url']),
            'thoi_gian_hoc' : str(row['course_time']),
            'ky_nang_khop'  : matched,
            'skill_match'   : int(row['skill_match'])
        })

    return {
        'nhom_sinh_vien' : GROUP_LABEL[group_pred],
        'nhom_id'        : group_pred,
        'do_kho_phu_hop' : difficulties,
        'ky_nang_dau_vao': input_skills,
        'ky_nang_mo_rong': expanded_skills[:8],
        'khoa_hoc_goi_y' : courses_out
    }


# ════════════════════════════════════════════════════════════
# FLASK ROUTES
# ════════════════════════════════════════════════════════════

@app.route('/recommend', methods=['POST'])
def api_recommend():
    """POST /recommend — Gợi ý khóa học theo 17 đặc trưng + kỹ năng"""
    try:
        body           = request.get_json()
        encoded_vector = body.get('encoded_vector', [])
        input_skills   = body.get('input_skills', [])
        top_n          = int(body.get('top_n', 5))

        print(f"🔍 encoded_vector: {encoded_vector}", flush=True)
        print(f"🔍 input_skills  : {input_skills}",   flush=True)

        if len(encoded_vector) != len(FEATURE_COLS):
            return jsonify({
                'success': False,
                'error'  : f'encoded_vector cần {len(FEATURE_COLS)} phần tử, nhận {len(encoded_vector)}'
            }), 400

        result = recommend(encoded_vector, input_skills, top_n)
        print(f"✅ nhóm={result['nhom_sinh_vien']} | khóa={len(result['khoa_hoc_goi_y'])}", flush=True)

        # Map sang format mà Spring Boot / recommend.html đang dùng
        return jsonify({
            'success'        : True,
            'grade'          : result['nhom_sinh_vien'],
            'predicted_score': [55, 65, 78, 90][result['nhom_id']],  # Điểm trung bình nhóm
            'recommendations': [
                {
                    'course_name' : c['ten_khoa_hoc'],
                    'category'    : c['to_chuc'],
                    'difficulty'  : c['cap_do'],
                    'rating'      : c['diem_danh_gia'],
                    'url'         : c['duong_dan'],
                    'match_score' : round(c['skill_match'] / max(len(expanded_skills := result['ky_nang_mo_rong']), 1), 2),
                    'matched_skills': c['ky_nang_khop']
                }
                for c in result['khoa_hoc_goi_y']
            ],
            'data'           : result  # Giữ lại toàn bộ data gốc
        })

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'message': 'Flask API đang chạy ✅', 'model': 'KNN+Apriori'})


@app.route('/skills', methods=['GET'])
def list_skills():
    """Trả về top 50 kỹ năng phổ biến nhất trong dataset khóa học"""
    skill_count = {}
    for lst in df_course['skills_parsed']:
        for sk in lst:
            skill_count[sk] = skill_count.get(sk, 0) + 1
    top_skills = sorted(skill_count.items(), key=lambda x: x[1], reverse=True)[:50]
    return jsonify({'skills': [s for s, _ in top_skills], 'total': len(top_skills)})


@app.route('/feature-cols', methods=['GET'])
def feature_cols():
    """Trả về danh sách 17 feature theo thứ tự — Spring Boot dùng để verify"""
    return jsonify({'feature_cols': FEATURE_COLS, 'total': len(FEATURE_COLS)})


# ── EDA ──────────────────────────────────────────────────────
sns.set_theme(style='whitegrid', palette='muted', font_scale=1.0)

class SafeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (np.integer,)): return int(obj)
        if isinstance(obj, (np.floating,)): return float(obj)
        if isinstance(obj, np.ndarray): return obj.tolist()
        if isinstance(obj, frozenset): return list(obj)
        return super().default(obj)

def safe_jsonify(data):
    return app.response_class(
        response=json.dumps(data, cls=SafeEncoder, ensure_ascii=False),
        status=200, mimetype='application/json'
    )

def fig_to_b64(fig):
    buf = io.BytesIO()
    fig.savefig(buf, format='png', bbox_inches='tight', dpi=90)
    plt.close(fig)
    buf.seek(0)
    return 'data:image/png;base64,' + base64.b64encode(buf.read()).decode()


@app.route('/eda', methods=['GET'])
def api_eda():
    """GET /eda?dataset=students|courses — Trả về biểu đồ EDA dạng base64"""
    dataset = request.args.get('dataset', 'students').lower()
    try:
        charts = []
        if dataset == 'students':
            df = df_student.copy()
            cont = ['Hours_Studied','Attendance','Previous_Scores','Sleep_Hours','Tutoring_Sessions','Physical_Activity']
            fig, axes = plt.subplots(2, 3, figsize=(14, 7))
            fig.suptitle('Phân phối các biến liên tục', fontweight='bold')
            for ax, feat in zip(axes.flatten(), cont):
                data = df[feat].dropna()
                ax.hist(data, bins=20, edgecolor='white', color='steelblue', alpha=0.8, density=True)
                data.plot.kde(ax=ax, color='darkred', linewidth=1.5)
                ax.axvline(data.mean(), color='orange', linestyle='--', linewidth=1.2)
                ax.set_title(feat.replace('_',' '), fontsize=9)
            plt.tight_layout()
            charts.append({'title': 'Phân phối biến liên tục', 'image': fig_to_b64(fig)})

            # Tương quan
            fig, ax = plt.subplots(figsize=(12, 9))
            corr = df.select_dtypes(include='number').corr()
            mask = np.triu(np.ones_like(corr, dtype=bool))
            sns.heatmap(corr, mask=mask, annot=True, fmt='.2f', cmap='RdYlGn',
                        center=0, linewidths=0.4, ax=ax, annot_kws={'size': 7})
            ax.set_title('Ma trận tương quan', fontsize=12, fontweight='bold')
            plt.tight_layout()
            charts.append({'title': 'Ma trận tương quan', 'image': fig_to_b64(fig)})

        elif dataset == 'courses':
            df = df_course.drop(columns=['skills_parsed'], errors='ignore').copy()
            df['course_rating'] = pd.to_numeric(df['course_rating'], errors='coerce')
            df['course_students_enrolled'] = pd.to_numeric(df['course_students_enrolled'], errors='coerce')

            fig, axes = plt.subplots(1, 3, figsize=(14, 4))
            fig.suptitle('Phân phối biến số — Courses', fontweight='bold')
            rating = df['course_rating'].dropna()
            axes[0].hist(rating, bins=20, color='steelblue', edgecolor='white')
            axes[0].axvline(rating.mean(), color='red', linestyle='--')
            axes[0].set_title('Course Rating')
            axes[1].hist(np.log1p(df['course_students_enrolled'].dropna()), bins=25, color='coral', edgecolor='white')
            axes[1].set_title('Số học viên (log)')
            axes[2].hist(pd.to_numeric(df.get('course_reviews_num', pd.Series()), errors='coerce').dropna().apply(lambda x: np.log1p(x)), bins=25, color='mediumpurple', edgecolor='white')
            axes[2].set_title('Số đánh giá (log)')
            plt.tight_layout()
            charts.append({'title': 'Phân phối Courses', 'image': fig_to_b64(fig)})
        else:
            return safe_jsonify({'success': False, 'error': 'dataset phải là students hoặc courses'})

        return safe_jsonify({'success': True, 'dataset': dataset, 'data': {'charts': charts}})

    except Exception as e:
        import traceback
        traceback.print_exc()
        return safe_jsonify({'success': False, 'error': str(e)})


# ── ENTRY POINT ───────────────────────────────────────────────
if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    print(f"🚀 Flask API running on port {port}")
    app.run(host='0.0.0.0', port=port, debug=False)
