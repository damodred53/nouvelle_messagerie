'use strict';

import createDropdownMenu from './dropdownMenu.js';
import { getMessagesByConversationId } from './services.js';
import { getAllUsers, saveMessageToLocalStorage } from './services.js';

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

/**
 * Lorsque la page est chargée, nous devons afficher le menu déroulant des utilisateurs
 */
window.addEventListener('DOMContentLoaded', async () => {

    const allUsers = await getAllUsers();
    const menuContainer = document.getElementById('menu-container'); 
    const dropdownMenu = await createDropdownMenu(allUsers, (secondPersonName) => {
        selectedUsername = secondPersonName;
    });
    menuContainer.appendChild(dropdownMenu.dropdown);
});

/**
 * Lorsque l'utilisateur revient en ligne, nous devons envoyer les messages en attente
 */
window.addEventListener("online",  () => {
    const cachedMessages = JSON.parse(localStorage.getItem("offlineMessages")) || [];
    console.log('cachedMessages:', cachedMessages);
    if (cachedMessages.length > 0) {
        cachedMessages.forEach(message => {
            /* envoi des messages à la websocket */
            stompClient.send("/app/chat.sendPrivateMessage", {}, JSON.stringify(message));
        });
        localStorage.removeItem("offlineMessages");
    }
});

/**
 * Fonction gérant la connexion d'un utilisateur à une conversation.
 * @param {*} event 
 */
function connect(event) {

    username = document.querySelector('#name').value.trim();
    console.log('username:', username);
    console.log('selectedUsername:', selectedUsername);
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
    var date = now.toISOString().split('T')[0]; 
    var time = now.getHours().toString().padStart(2, '0') + ":" + now.getMinutes().toString().padStart(2, '0'); // HH:mm
    return { date: date, time: time };
}

const onConnected = async () => {
    // Exemple : l'utilisateur et le destinataire ont déjà été définis
    const conversationId = generateConversationId(username, selectedUsername);
    console.log('conversationId:', conversationId);
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

    /* Mettre à jour le titre au-dessus de la conversation */
    if (chatHeader) {
        chatHeader.innerHTML = `<h2>Conversation de ${username} et de ${selectedUsername}</h2>`
    }
};

/**
 * Création de l'ID de la conversation en fonction des noms d'utilisateur
 * @param {*} sender 
 * @param {*} recipient 
 * @returns 
 */
function generateConversationId(sender, recipient) {
    return sender.localeCompare(recipient) < 0
        ? `${sender.toLowerCase()}_${recipient.toLowerCase()}`
        : `${recipient.toLowerCase()}_${sender.toLowerCase()}`;
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

/**
 * Affichage des messages reçus depuis l'historique de la base de données
 * @param {*} messages 
 */
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

/**
 * Envoi de smessages depuis le local storage ou depuis le front-end vers l'API
 * @param {*} eventOrMessage 
 */
function sendMessage(eventOrMessage) {
    // Vérifier si eventOrMessage est un événement ou un message
    let messageContent;
    let isCachedMessage = false;

    if (eventOrMessage.preventDefault) {
        // Cas où la fonction est appelée comme gestionnaire d'événement
        eventOrMessage.preventDefault();
        messageContent = messageInput.value.trim();
    } else {
        // Cas où la fonction est appelée avec un message depuis le cache
        isCachedMessage = true;
        messageContent = eventOrMessage.content; 
    }

    if (messageContent && stompClient) {
        // Obtenir la date et l'heure actuelles si ce n'est pas un message caché
        const { date, time } = isCachedMessage ?
            { date: eventOrMessage.date, time: eventOrMessage.time } :
            getCurrentDateTime();

        const chatMessage = isCachedMessage
            ? eventOrMessage 
            : {
                sender: username.toLowerCase(),
                content: messageInput.value,
                type: 'CHAT',
                recipient: selectedUsername.toLowerCase(),
                date: date,
                time: time
            };

        // Générer l'ID de la conversation (topic privé)
        const conversationId = generateConversationId(username, selectedUsername);

        if (!navigator.onLine && !isCachedMessage) {
            saveMessageToLocalStorage(chatMessage);
            onMessageReceived({ body: JSON.stringify(chatMessage) });
        } else {
            stompClient.send("/app/chat.sendPrivateMessage", {}, JSON.stringify(chatMessage));
        }

        // Réinitialiser l'input uniquement pour un événement
        if (!isCachedMessage) {
            messageInput.value = '';
        }
    }
}

/**
 * Affichage des messages reçues de la web socket ou de la base de données
 * @param {*} payload 
 */
function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log('Message reçu:', message);

    var messageElement = document.createElement('li');
    messageElement.classList.add('chat-message');

    // Création de l'avatar
    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.sender[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender);

    messageElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(message.sender);
    var dateElement = document.createElement('span');
    var timeElement = document.createElement('span');

    // Formater la date et l'heure
    const formattedDateTime = formatMessageDate(message.date, message.time);

    // Date et heure en gris et plus petit
    dateElement.textContent = formattedDateTime;
    dateElement.style.fontSize = '0.8rem'; 
    dateElement.style.color = 'gray'; 

    // Ajouter le nom de l'utilisateur et séparer de deux espaces
    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);

    // Créer un espace pour séparer avec deux espaces
    var spaceElement = document.createElement('span');
    spaceElement.textContent = '  '; // Deux espaces
    messageElement.appendChild(spaceElement);

    messageElement.appendChild(dateElement);

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * 
 * @param {*} messageSender 
 * @returns 
 */
function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

/**
 * Lorsque l'utilisateur se déconnecte, nous devons envoyer un message de déconnexion à la websocket
 */
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

function getRelativeDate(messageDate) {
    const now = new Date();
    const messageDateObj = new Date(messageDate);

    // Comparer la date sans tenir compte de l'heure
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    // Formater la date du message
    if (messageDateObj >= today) {
        return "Aujourd'hui à";
    } else if (messageDateObj >= yesterday) {
        return "Hier à";
    } else {
        return messageDateObj.toLocaleDateString('fr-FR'); 
    }
}
function formatMessageDate(date, time) {
    const relativeDate = getRelativeDate(date); 
    return `${relativeDate} ${time}`; 
}
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
disconnect_button.addEventListener('click', onDisconnect, true);