<template>
  <div class="home">
    <div class="container">
      <div class="hero-section">
        <h1 class="title">DDLWLRMA AI Agent</h1>
        <p class="subtitle">选择您想要体验的AI应用</p>
        
        <div class="app-cards">
          <div class="app-card" @click="goToLoveChat">
            <div class="card-icon">💕</div>
            <h3 class="card-title">AI 恋爱大师</h3>
            <p class="card-description">专业的恋爱咨询和情感建议，让您的感情生活更加美好</p>
            <div class="card-button">
              <span>开始聊天</span>
              <span class="arrow">→</span>
            </div>
          </div>
          
          <div class="app-card" @click="goToManusChat">
            <div class="card-icon">🤖</div>
            <h3 class="card-title">"世另我"AI 超级智能体</h3>
            <p class="card-description">强大的AI助手，为您提供全方位的智能服务和解决方案</p>
            <div class="card-button">
              <span>开始聊天</span>
              <span class="arrow">→</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
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
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 1200px;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  color: white;
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
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 40px;
  max-width: 900px;
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
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 1);
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

@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1.2rem;
  }
  
  .app-cards {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .app-card {
    padding: 30px 20px;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
}
</style>
