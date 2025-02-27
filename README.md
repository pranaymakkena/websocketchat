# WebSocket Chat Application

A real-time chat application using Java WebSocket for the backend and vanilla JavaScript for the frontend.

## 📂 Project Structure
```
websocketchat/
│── src/            # Backend (Java WebSocket Server)
│   ├── lib/        # Dependencies
│   ├── ChatServer.java  # WebSocket Server Code
│   ├── Dockerfile      # Docker Configuration
│── web/            # Frontend (HTML, CSS, JS)
│   ├── index.html  # Chat UI
│   ├── style.css   # Styling
│   ├── script.js   # WebSocket Client
│── README.md       # Project Documentation
```

## 🚀 Installation & Setup
### 1️⃣ Clone the Repository
```sh
git clone https://github.com/pranaymakkena/websocketchat.git
cd websocketchat
```

### 2️⃣ Run the Backend Server
#### Using Java (Standalone)
```sh
cd src
javac -cp .:lib/* ChatServer.java
java -cp .:lib/* ChatServer
```

#### Using Docker
```sh
docker build -t chat-server .
docker run -p 8080:8080 chat-server
```

### 3️⃣ Run the Frontend
Simply open `web/index.html` in your browser.

## 🌍 Deployment (Render)
### 1️⃣ Backend Deployment
1. Create a **new Web Service** on [Render](https://render.com/)
2. Use this **Dockerfile**:
```dockerfile
FROM openjdk:11
WORKDIR /app
COPY src/ /app/
RUN javac -cp .:lib/* ChatServer.java
CMD ["java", "-cp", ".:lib/*", "ChatServer"]
EXPOSE 8080
```
3. Set **PORT=8080** in environment variables.

### 2️⃣ Frontend Deployment
1. Host `web/` folder on **GitHub Pages**, **Vercel**, or **Netlify**.
2. Update `script.js` WebSocket URL:
```js
const socket = new WebSocket('wss://your-backend.onrender.com');
```

## 🛠 Features 
✅ Real-time messaging  
✅ User joins/leaves notification  
✅ Random color assignment per user  
✅ WebSocket-based communication  
