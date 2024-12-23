'use strict';

import  createDropdownMenu  from './dropdownMenu.js';
import { getAllUsers } from './services.js';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var disconnect_button = document.querySelector('.disconnect_button');

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
        console.log('Utilisateur sélectionné (callback dans main.js):', selectedUsername);
    });
    menuContainer.appendChild(dropdownMenu.dropdown);
    
});


// // Ajouter l'écouteur d'événement pour la fermeture de la fenêtre
// disconnect_button.addEventListener('click', onDisconnected, true);

function connect(event) {
    console.log(event);
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket); 

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function getCurrentDateTime() {
    var now = new Date();

    // Formater la date au format YYYY-MM-DD
    var date = now.toISOString().split('T')[0]; // Partie date de la ISO 8601 (YYYY-MM-DD)

    // Formater l'heure au format HH:mm
    var time = now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0'); // HH:mm
    return { date: date, time: time };
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);
console.log("le destinataire ", selectedUsername);
    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN', date: getCurrentDateTime().date, time: getCurrentDateTime().time})
    )

    const conversationIdGeneration = generateConversationId(username, selectedUsername);
    console.log("voici mon mot de passe : ", conversationIdGeneration);

    connectingElement.classList.add('hidden');
}

function generateConversationId(sender, recipient) {
    // S'assurer que l'ordre est alphabétique pour garantir l'unicité
    return sender.localeCompare(recipient) < 0
        ? `${sender}_${recipient}`
        : `${recipient}_${sender}`;
}

function onDisconnected() {
    // Informer le serveur que l'utilisateur quitte
    stompClient.send("/chat.removeUser",
        {},
        JSON.stringify({sender: username, type: 'LEAVE', date: getCurrentDateTime().date, time: getCurrentDateTime().time})
    );

    // Déconnexion de la WebSocket
    stompClient.disconnect(function() {
        console.log("Disconnected");
        // Cacher la page de chat et afficher à nouveau la page de connexion
        chatPage.classList.add('hidden');
        usernamePage.classList.remove('hidden');
        connectingElement.classList.add('hidden');
    });
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}




function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        // Appeler la fonction pour obtenir la date et l'heure formatées
        var { date, time } = getCurrentDateTime();

        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT',
            recipient: 'JohnDoe', // Remplace par le destinataire réel
            date: date,  // Date au format YYYY-MM-DD
            time: time   // Heure au format HH:mm
        };

        console.log(chatMessage);

        // Envoi des messages via WebSocket
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        stompClient.send("/app/chat.sendPrivateMessage", {}, JSON.stringify(chatMessage));

        // Réinitialisation du champ message
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log("voici le message : ", payload);
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

        timeElement.appendChild(dateText);
        messageElement.appendChild(dateElement);

        timeElement.appendChild(timeText);
        messageElement.appendChild(timeElement);
    

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
