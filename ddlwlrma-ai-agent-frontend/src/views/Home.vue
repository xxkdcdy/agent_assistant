<template>
  <div class="home">
    <div class="container">
      <div class="hero-section">
        <div class="hero-avatar">
          <WebsiteAvatar size="large" theme="gradient" />
        </div>
        <h1 class="title">DDLWLRMA AI Agent</h1>
        <p class="subtitle">选择您想要体验的AI应用</p>
        
        <div class="app-cards">
          <div class="app-card" @click="goToLoveChat">
            <div class="card-content">
              <div class="card-icon">💕</div>
              <h3 class="card-title">AI 恋爱大师</h3>
              <p class="card-description">专业的恋爱咨询和情感建议，让您的感情生活更加美好</p>
            </div>
            <div class="card-button">
              <span>开始聊天</span>
              <span class="arrow">→</span>
            </div>
          </div>
          
          <div class="app-card" @click="goToManusChat">
            <div class="card-content">
              <div class="card-icon">🤖</div>
              <h3 class="card-title">"世另我"AI 智能体</h3>
              <p class="card-description">强大的AI助手，为您提供全方位的智能服务和解决方案</p>
            </div>
            <div class="card-button">
              <span>开始聊天</span>
              <span class="arrow">→</span>
            </div>
          </div>
          
          <div class="app-card coming-soon">
            <div class="card-content">
              <div class="card-icon">🚀</div>
              <h3 class="card-title">更多AI应用</h3>
              <p class="card-description">更多强大的AI工具和服务即将上线，敬请期待</p>
            </div>
            <div class="card-button coming-soon-btn">
              <span>Coming Soon</span>
              <span class="arrow">⏳</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 网站底部信息 -->
      <div class="footer">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>DDLWLRMA AI Agent 致力于提供最先进的人工智能服务</p>
          </div>
          <div class="footer-section">
            <h4>联系方式</h4>
            <p>Email: donggua513@163.com</p>
            <p><a href="#" class="footer-link">个人主页</a></p>
          </div>
          <div class="footer-section">
            <h4>服务条款</h4>
            <p><a href="#" class="footer-link">用户协议</a></p>
            <p><a href="#" class="footer-link">隐私政策</a></p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 DDLWLRMA AI Agent. 保留所有权利.</p>
          <p class="footer-tech">Powered by Vue.js & Spring AI</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import WebsiteAvatar from '../components/WebsiteAvatar.vue'

export default {
  name: 'Home',
  components: {
    WebsiteAvatar
  },
  methods: {
    goToLoveChat() {
      this.$router.push('/love-chat')
    },
    goToManusChat() {
      this.$router.push('/manus-chat')
    }
  }
}
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 20px;
  box-sizing: border-box;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-section {
  text-align: center;
  color: white;
}

.hero-avatar {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 1.5rem;
  margin-bottom: 60px;
  opacity: 0.9;
  font-weight: 300;
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.app-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 350px;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 1);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.card-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.card-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2c3e50;
}

.card-description {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #666;
  margin-bottom: 30px;
}

.card-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.app-card:hover .card-button {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: scale(1.05);
}

.arrow {
  transition: transform 0.3s ease;
}

.app-card:hover .arrow {
  transform: translateX(4px);
}

/* Coming Soon 卡片特殊样式 */
.app-card.coming-soon {
  cursor: default;
  opacity: 0.8;
}

.app-card.coming-soon:hover {
  transform: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.95);
}

.coming-soon-btn {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
  cursor: not-allowed;
}

.app-card.coming-soon:hover .coming-soon-btn {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
  transform: none;
}

.app-card.coming-soon .arrow {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 底部信息样式 */
.footer {
  margin-top: 60px;
  padding: 40px 0 20px;
  color: rgba(255, 255, 255, 0.9);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
  margin-bottom: 30px;
}

.footer-section h4 {
  color: white;
  font-size: 1.2rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.footer-section p {
  margin-bottom: 8px;
  font-size: 0.9rem;
  line-height: 1.5;
  opacity: 0.8;
}

.footer-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: white;
  text-decoration: underline;
}

.footer-bottom {
  text-align: center;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-bottom p {
  margin: 5px 0;
  font-size: 0.85rem;
  opacity: 0.7;
}

.footer-tech {
  color: rgba(255, 255, 255, 0.6);
  font-style: italic;
}

/* 平板设备优化 */
@media (max-width: 1024px) and (min-width: 769px) {
  .container {
    padding: 30px;
  }
  
  .app-cards {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    max-width: 900px;
  }
  
  .app-card {
    padding: 30px 20px;
    min-height: 320px;
  }
  
  .card-title {
    font-size: 1.4rem;
  }
  
  .card-description {
    font-size: 1rem;
  }
  
  .footer-content {
    gap: 30px;
  }
}

/* 移动设备优化 */
@media (max-width: 768px) {
  .home {
    min-height: 100dvh; /* 动态视口高度，支持移动设备地址栏 */
  }
  
  .container {
    padding: 20px 15px;
  }
  
  .title {
    font-size: 2.2rem;
    margin-bottom: 15px;
  }
  
  .hero-avatar {
    margin-bottom: 20px;
  }
  
  .subtitle {
    font-size: 1.1rem;
    margin-bottom: 40px;
  }
  
  .app-cards {
    grid-template-columns: 1fr;
    gap: 20px;
    max-width: 400px;
  }
  
  .app-card {
    padding: 25px 20px;
    border-radius: 15px;
    min-height: 280px;
  }
  
  .card-icon {
    font-size: 3rem;
    margin-bottom: 15px;
  }
  
  .card-title {
    font-size: 1.4rem;
    margin-bottom: 12px;
  }
  
  .card-description {
    font-size: 1rem;
    margin-bottom: 25px;
  }
  
  .footer {
    margin-top: 40px;
    padding: 30px 0 15px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 25px;
    text-align: center;
  }
  
  .footer-section h4 {
    font-size: 1.1rem;
    margin-bottom: 12px;
  }
  
  .footer-section p {
    font-size: 0.85rem;
  }
  
  .footer-bottom {
    padding: 15px;
  }
  
  .footer-bottom p {
    font-size: 0.8rem;
  }
}

/* 超小屏设备 */
@media (max-width: 480px) {
  .container {
    padding: 15px 10px;
  }
  
  .title {
    font-size: 1.8rem;
  }
  
  .subtitle {
    font-size: 1rem;
    margin-bottom: 30px;
  }
  
  .app-cards {
    gap: 15px;
    max-width: 350px;
  }
  
  .app-card {
    padding: 20px 15px;
    min-height: 250px;
  }
  
  .card-icon {
    font-size: 2.5rem;
  }
  
  .card-title {
    font-size: 1.2rem;
  }
  
  .card-description {
    font-size: 0.9rem;
  }
  
  .footer {
    margin-top: 30px;
    padding: 20px 0 10px;
  }
  
  .footer-content {
    gap: 20px;
    padding: 0 10px;
  }
  
  .footer-section h4 {
    font-size: 1rem;
  }
  
  .footer-section p {
    font-size: 0.8rem;
  }
  
  .footer-bottom p {
    font-size: 0.75rem;
  }
}
</style>
