@echo off
title EduRecommend Packaged Runner
echo ============================================================
echo   Dong goi Frontend vao Backend va khoi chay 1 duong dan duy nhat
echo ============================================================

echo 1. Dang build Frontend (Vue 3 SPA)...
cd frontend-vue
call npm install
call npm run build
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Build Frontend that bai! Vui long kiem tra nodejs/npm.
    pause
    exit /b %ERRORLEVEL%
)
cd ..

echo 2. Dang lam sach va sao chep ban build Frontend sang thu muc static cua Backend...
if not exist "Edu_Recommend\doan\src\main\resources\static" (
    mkdir "Edu_Recommend\doan\src\main\resources\static"
)
del /q /s "Edu_Recommend\doan\src\main\resources\static\*.*" >nul 2>&1
xcopy /y /s /e "frontend-vue\dist\*" "Edu_Recommend\doan\src\main\resources\static\"

echo 3. Dang khoi dong Spring Boot Backend (Gom ca Frontend ben trong)...
cd Edu_Recommend\doan
call mvnw.cmd spring-boot:run
