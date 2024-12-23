
export const getAllUsers = async () => {
    try {
        const allUsersResponse = await fetch('/api/users');
        const data = await allUsersResponse.json();  // Attendre que la réponse JSON soit traitée
        // console.log('Tous les utilisateurs:', data);

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
    const apiUrl = `/api/chat/messages?conversationId=${encodeURIComponent(conversationId)}`;
    console.log("la fonction est appelée")
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


