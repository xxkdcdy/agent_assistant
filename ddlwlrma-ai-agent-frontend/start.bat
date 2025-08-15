@echo off
echo 正在安装依赖...
call npm install

echo.
echo 启动开发服务器...
echo 访问地址：http://localhost:3000
echo.
call npm run dev

pause
