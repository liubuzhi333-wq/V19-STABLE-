# 36.5° 数字温控系统 — V19 Stable Touch + Boot

本工程严格以已经实机验证流畅的 V19 Native 版为运行基准。

## 保持 V19 不变的部分
- Android 原生 1920×1080 设计坐标系
- 蓝灰色整体视觉与排版
- Animated WebP 模特播放 + ZIP 逐帧备用播放器
- 4 张热力图原生 ImageView 轮播（1.2 秒）
- 播放 / 暂停 / 重置、四视角按钮
- 沉浸式横屏、常亮、设备诊断页

## 本版仅增加/修改
1. **媒体触控层**：在 V19 的 Model/Thermal ImageView 上增加透明 Gesture Overlay。触控对象与动画/图片对象完全分离，避免图片刷新影响手势；支持双指缩放、放大后单指拖动、双击复位。
2. **多点事件不拆分**：DashboardLayout 关闭 MotionEvent splitting，适配大尺寸 USB/HID 触摸框。
3. **开机自启双保险**：保留 BOOT_COMPLETED Receiver，并增加可选 Android HOME/Launcher 模式。专用展示终端可把本 APP 设为默认“主屏幕应用”。
4. **隐藏诊断**：连续点击左上标题 5 次可查看 Model Touch Max / Thermal Touch Max 与屏幕参数。

## 推荐实机验证顺序
- 先验证 V19 原有流畅度、模特动画、热力轮播是否保持。
- 在左/右媒体框内双指缩放，再单指拖动；双击应复位。
- 缩放热力图后轮播应继续。
- 连点标题 5 次：若 `Model Touch Max` / `Thermal Touch Max` 为 2 或以上，说明多点事件已进入 APP。
- 展示终端可在系统设置中将本 APP 设为默认 Home/Launcher，实现更可靠的通电自启。

## GitHub 上传
由于浏览器上传可能拍平 Android 目录，请不要解压后逐文件上传。使用外层提供的 `ThermalSuit_V19_STABLE_PROJECT.zip` 作为单个文件上传，并使用配套 workflow 自动解压构建。
