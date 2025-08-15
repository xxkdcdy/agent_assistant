<template>
  <div class="website-avatar" :class="[size, theme]">
    <div class="avatar-content">
             <div class="avatar-icon">
         <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" class="rocket-svg">
           <defs>
             <linearGradient id="avatarRocketGradient" x1="0%" y1="0%" x2="100%" y2="100%">
               <stop offset="0%" style="stop-color:#f093fb;stop-opacity:1" />
               <stop offset="50%" style="stop-color:#f5576c;stop-opacity:1" />
               <stop offset="100%" style="stop-color:#4facfe;stop-opacity:1" />
             </linearGradient>
           </defs>
           <!-- 火箭主体 -->
           <path d="M50 15 L35 25 L35 75 L65 75 L65 25 Z" fill="url(#avatarRocketGradient)"/>
           <!-- 火箭头部 -->
           <path d="M50 15 L40 25 L60 25 Z" fill="#f093fb"/>
           <!-- 火箭尾部 -->
           <path d="M35 75 L50 85 L65 75 Z" fill="#4facfe"/>
           <!-- 火箭窗口 -->
           <circle cx="50" cy="35" r="3" fill="rgba(255,255,255,0.8)"/>
           <circle cx="50" cy="50" r="3" fill="rgba(255,255,255,0.8)"/>
           <circle cx="50" cy="65" r="3" fill="rgba(255,255,255,0.8)"/>
           <!-- 火箭火焰 -->
           <path d="M35 85 L50 95 L65 85 L60 90 L50 88 L40 90 Z" fill="#ff6b35"/>
           <path d="M40 90 L50 92 L60 90 L55 94 L50 92 L45 94 Z" fill="#ff8c42"/>
         </svg>
       </div>
      <div class="avatar-glow"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WebsiteAvatar',
  props: {
    size: {
      type: String,
      default: 'medium',
      validator: value => ['small', 'medium', 'large'].includes(value)
    },
    theme: {
      type: String,
      default: 'default',
      validator: value => ['default', 'gradient', 'neon', 'minimal'].includes(value)
    }
  }
}
</script>

<style scoped>
.website-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-icon {
  position: relative;
  z-index: 2;
  font-size: 1.2em;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  display: flex;
  align-items: center;
  justify-content: center;
}

.rocket-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.avatar-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  z-index: 1;
}

/* 尺寸变体 */
.small .avatar-content {
  width: 32px;
  height: 32px;
}

.small .avatar-icon {
  font-size: 1em;
}

.medium .avatar-content {
  width: 48px;
  height: 48px;
}

.medium .avatar-icon {
  font-size: 1.5em;
}

.large .avatar-content {
  width: 64px;
  height: 64px;
}

.large .avatar-icon {
  font-size: 2em;
}

/* 主题变体 */
.default .avatar-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.default .avatar-glow {
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
}

.gradient .avatar-content {
  background: linear-gradient(45deg, #f093fb 0%, #f5576c 50%, #4facfe 100%);
  box-shadow: 0 6px 16px rgba(240, 147, 251, 0.4);
}

.gradient .avatar-glow {
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 60%);
  animation: gradientGlow 3s ease-in-out infinite;
}

.neon .avatar-content {
  background: linear-gradient(135deg, #00d4ff 0%, #0099cc 100%);
  box-shadow: 
    0 0 20px rgba(0, 212, 255, 0.5),
    0 0 40px rgba(0, 212, 255, 0.3),
    inset 0 0 20px rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(0, 212, 255, 0.8);
}

.neon .avatar-glow {
  background: radial-gradient(circle, rgba(0, 212, 255, 0.4) 0%, transparent 70%);
  animation: neonPulse 2s ease-in-out infinite;
}

.minimal .avatar-content {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #dee2e6;
}

.minimal .avatar-icon {
  color: #6c757d;
}

@keyframes gradientGlow {
  0%, 100% { 
    opacity: 0.3;
    transform: scale(1);
  }
  50% { 
    opacity: 0.6;
    transform: scale(1.1);
  }
}

@keyframes neonPulse {
  0%, 100% { 
    opacity: 0.4;
    transform: scale(1);
  }
  50% { 
    opacity: 0.8;
    transform: scale(1.05);
  }
}

/* 悬停效果 */
.website-avatar:hover .avatar-content {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

.website-avatar:hover .avatar-glow {
  animation-duration: 1.5s;
}
</style>
