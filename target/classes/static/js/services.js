
export const getAllUsers = async () => {
    try {
        const allUsersResponse = await fetch('/api/users');
        const data = await allUsersResponse.json();  

        return data;
    } catch (error) {
        console.error('Erreur lors de la récupération des utilisateurs:', error);
    }
    
}



/**
 * Récupère les messages pour un conversationId spécifique via une requête GET.
 * @param {string} conversationId - L'identifiant unique de la conversation.
 * @returns {Promise<Array>} - Une promesse résolue avec une liste de messages.
 */
export async function getMessagesByConversationId(conversationId) {
    const apiUrl = `/api/messages/by-conversation?conversationId=${encodeURIComponent(conversationId)}`;

    try {
        const response = await fetch(apiUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Erreur HTTP ${response.status}: Impossible de récupérer les messages.`);
        }

        const messages = await response.json();
        console.log('Messages récupérés:', messages);
        return messages;
    } catch (error) {
        console.error('Erreur lors de la récupération des messages:', error);
        throw error;
    }
}

function generateConversationId(sender, recipient) {
    return sender.localeCompare(recipient) < 0
        ? `${sender.toLowerCase()}_${recipient.toLowerCase()}`
        : `${recipient.toLowerCase()}_${sender.toLowerCase()}`;
}

export function saveMessageToLocalStorage(message) {
    console.log('Enregistrement du message dans le cache local:', message);
    const cachedMessages = JSON.parse(localStorage.getItem("offlineMessages")) || [];
    cachedMessages.push(message);
    localStorage.setItem("offlineMessages", JSON.stringify(cachedMessages));
}



