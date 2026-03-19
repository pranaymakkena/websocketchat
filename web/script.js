// Determine correct WebSocket URL (local vs production)
function getWebSocketURL() {
    const isLocal = window.location.hostname === "localhost";

    if (isLocal) {
        return "ws://localhost:8080";
    }

    return "wss://websocketchat-vg29.onrender.com";
}

// Establish WebSocket connection
let socket = new WebSocket(getWebSocketURL());

// DOM elements
const chatBox = document.getElementById('chat-box');
const messageInput = document.getElementById('message');
const nameInput = document.getElementById('name');
const sendButton = document.getElementById('send');

let userName = '';
let hasJoinedChat = false;

// Escape HTML (prevents XSS attacks)
function escapeHTML(text) {
    const div = document.createElement("div");
    div.innerText = text;
    return div.innerHTML;
}

// Append messages to chat UI
function appendMessage(name, message, time, color, type = 'normal') {
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');

    if (type === 'join') {
        messageDiv.classList.add('join-message');
        messageDiv.innerHTML = `<strong>${escapeHTML(name)}</strong> has joined the chat.`;
    } else if (type === 'leave') {
        messageDiv.classList.add('leave-message');
        messageDiv.innerHTML = `<strong>${escapeHTML(name)}</strong> has left the chat.`;
    } else {
        messageDiv.innerHTML = `
            <div class="username">${escapeHTML(name)}</div>
            <div class="message-text" style="background-color: ${color || '#f0f0f0'};">
                ${escapeHTML(message)}
            </div>
            <span class="timestamp">${time}</span>
        `;
    }

    chatBox.appendChild(messageDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// Send button click
sendButton.addEventListener('click', () => {
    if (!hasJoinedChat) {
        userName = nameInput.value.trim();

        if (userName) {
            socket.send(`name:${userName}`);
            nameInput.disabled = true;
        } else {
            alert('Please enter your name before joining the chat.');
        }

    } else {
        const message = messageInput.value.trim();

        if (message) {
            socket.send(`message:${message}`);
            messageInput.value = '';
        }
    }
});

// Receive messages
socket.addEventListener('message', (event) => {
    try {
        const data = JSON.parse(event.data);

        if (data.type === 'ask_name') {
            nameInput.disabled = false;
            nameInput.focus();

        } else if (data.type === 'start_chat') {
            hasJoinedChat = true;

            document.getElementById('join-section').style.display = 'none';
            document.getElementById('chat-section').style.display = 'block';

            messageInput.disabled = false;
            sendButton.disabled = false;

        } else if (data.type === 'message') {
            appendMessage(data.name, data.message, data.time, data.color);

        } else if (data.type === 'join') {
            appendMessage(data.name, '', '', null, 'join');

        } else if (data.type === 'leave') {
            appendMessage(data.name, '', '', null, 'leave');
        }

    } catch (error) {
        console.error('Error parsing message:', error);
    }
});

// Enter key support
messageInput.addEventListener('keypress', (event) => {
    if (event.key === 'Enter' && !messageInput.disabled) {
        sendButton.click();
    }
});

nameInput.addEventListener('keypress', (event) => {
    if (event.key === 'Enter' && !nameInput.disabled) {
        sendButton.click();
    }
});

// Connection events
socket.addEventListener('open', () => {
    console.log('✅ Connected to WebSocket server');
});

socket.addEventListener('close', () => {
    console.log('❌ Disconnected from server');

    // Auto-reconnect (simple version)
    setTimeout(() => {
        location.reload();
    }, 2000);
});

socket.addEventListener('error', (error) => {
    console.error('⚠️ WebSocket error:', error);
});