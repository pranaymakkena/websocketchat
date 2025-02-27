// Establish WebSocket connection dynamically
const socket = new WebSocket("wss://websocketchat-vg29.onrender.com");

// DOM elements
const chatBox = document.getElementById('chat-box');
const messageInput = document.getElementById('message');
const nameInput = document.getElementById('name');
const sendButton = document.getElementById('send');
const typingIndicator = document.getElementById('typing-indicator');

let userName = '';
let hasJoinedChat = false;

// Function to determine WebSocket URL dynamically
function getWebSocketURL() {
    let url = window.location.origin.replace(/^http/, 'ws'); // Auto-adjust for WebSocket
    return `${url}`;
}

// Function to append messages to chat box
function appendMessage(name, message, time, color, type = 'normal') {
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');

    if (type === 'join') {
        messageDiv.classList.add('join-message');
        messageDiv.innerHTML = `<strong>${name}</strong> has joined the chat.`;
    } else if (type === 'leave') {
        messageDiv.classList.add('leave-message');
        messageDiv.innerHTML = `<strong>${name}</strong> has left the chat.`;
    } else {
        messageDiv.innerHTML = `
            <div class="username">${name}</div>
            <div class="message-text" style="background-color: ${color || '#f0f0f0'};">
                ${message}
            </div>
            <span class="timestamp">${time}</span>
        `;
    }

    chatBox.appendChild(messageDiv);
    chatBox.scrollTop = chatBox.scrollHeight; // Auto scroll to bottom
}

// Send message when the send button is clicked
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

// Listen for incoming messages
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
            appendMessage(userName, 'has joined the chat', new Date().toLocaleTimeString(), null, 'join');
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

// Enter key functionality for sending messages
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

// Handle WebSocket connection events
socket.addEventListener('open', () => {
    console.log('Connected to WebSocket server');
});

socket.addEventListener('close', () => {
    console.log('Disconnected from WebSocket server');
    alert('Connection to chat server lost. Please refresh the page.');
});

socket.addEventListener('error', (error) => {
    console.error('WebSocket error:', error);
});
