'use strict';

import createDropdownMenu from './dropdownMenu.js';
import { getMessagesByConversationId } from './services.js';
import { getAllUsers } from './services.js';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var disconnect_button = document.querySelector('.disconnect_button');
const chatHeader = document.querySelector('.chat_header');


var stompClient = null;
var username = null;
let selectedUsername = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

window.addEventListener('DOMContentLoaded', async () => {
    
    const allUsers = await getAllUsers();
    const menuContainer = document.getElementById('menu-container'); // Assurez-vous que cet élément existe dans votre HTML
    const dropdownMenu = await createDropdownMenu(allUsers, (secondPersonName) => {
        selectedUsername = secondPersonName;
    });
    //  updateOpeningRoomButton();
    menuContainer.appendChild(dropdownMenu.dropdown);
});


function connect(event) {
    
    username = document.querySelector('#name').value.trim();
    console.log('username:', username);
    console.log('selectedUsername:', selectedUsername);
    //    updateOpeningRoomButton();
    if (username && selectedUsername) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket); 

        stompClient.connect({}, onConnected, onError);
    } else {
        alert('Veuillez saisir un nom d\'utilisateur et sélectionner un destinataire.');
    }
    event.preventDefault();
}

function getCurrentDateTime() {
    var now = new Date();
    var date = now.toISOString().split('T')[0]; // Partie date de la ISO 8601 (YYYY-MM-DD)
    var time = now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0'); // HH:mm
    return { date: date, time: time };
}

const onConnected = async () => {
    // Exemple : l'utilisateur et le destinataire ont déjà été définis
    const conversationId = generateConversationId(username, selectedUsername);

    // Abonner l'utilisateur au topic privé de la conversation
    stompClient.subscribe(`/topic/${conversationId}`, onMessageReceived);

    // Envoi d'un message indiquant que l'utilisateur a rejoint la conversation
    const response = await stompClient.send(`/app/chat.addUser/${conversationId}`, {}, JSON.stringify({
        sender: username,
        type: 'JOIN',
        date: getCurrentDateTime().date,
        time: getCurrentDateTime().time
    }));

    
    
    // Récupérer les anciens messages pour afficher l'historique
    const conversationIdGeneration = await getMessagesByConversationId(conversationId);
    displayAllOldMessages(conversationIdGeneration);

    connectingElement.classList.add('hidden');

    if (chatHeader) {
        chatHeader.innerHTML = `<h2>Conversation de ${username} et de ${selectedUsername}</h2>`
    }
};

function generateConversationId(sender, recipient) {
    return sender.localeCompare(recipient) < 0
        ? `${sender}_${recipient}`
        : `${recipient}_${sender}`;
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

const displayAllOldMessages = (messages) => {
    console.log("Messages reçus pour l'historique :", messages);
    messages.sort((a, b) => {
        const dateA = new Date(a.date + 'T' + a.time);
        const dateB = new Date(b.date + 'T' + b.time);
        return dateA.getTime() - dateB.getTime();
    });

    messages.forEach(message => {
        onMessageReceived({ body: JSON.stringify(message) });
    });
};

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        // Obtenir la date et l'heure actuelles
        var { date, time } = getCurrentDateTime();

        // Créer un message
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT',
            recipient: selectedUsername, // Le destinataire du message
            date: date,
            time: time
        };

        // Générer l'ID de la conversation (topic privé)
        const conversationId = generateConversationId(username, selectedUsername);

        // Envoyer le message via WebSocket au topic privé
        stompClient.send("/app/chat.sendPrivateMessage", {}, JSON.stringify(chatMessage));

        // Réinitialiser l'input
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log('Message reçu:', message);

    var messageElement = document.createElement('li');
    messageElement.classList.add('chat-message');


    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.sender[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender);

    messageElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(message.sender);
    var dateElement = document.createElement('span');
    var dateText = document.createTextNode(message.date);
    var timeElement = document.createElement('span');
    var timeText = document.createTextNode(message.time);

    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);

    dateElement.appendChild(dateText);
    messageElement.appendChild(dateElement);

    timeElement.appendChild(timeText);
    messageElement.appendChild(timeElement);

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight; // Faire défiler vers le bas
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

const onDisconnect = async () => {
    if (stompClient) {
        const conversationId = generateConversationId(username, selectedUsername);
        const response = await stompClient.send(`/app/chat.removeUser/${conversationId}`, {}, JSON.stringify({
            sender: username,
            type: 'JOIN',
            date: getCurrentDateTime().date,
            time: getCurrentDateTime().time
        }));

            stompClient.disconnect();
    }
    window.location.reload();
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
disconnect_button.addEventListener('click', onDisconnect, true);
