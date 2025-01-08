'use strict';



/**
 * Gestionnaire du menu déroulant pour la sélection d'un utilisateur à l'écran d'accueil
 * @param {*} allUsers 
 * @param {*} onUserSelected 
 * @returns 
 */
const createDropdownMenu = async (allUsers, onUserSelected)  => {

    let selectedUsername = null;

    const dropdown = document.createElement('select');
    dropdown.id = 'dynamic-dropdown';
    

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Sélectionner un utilisateur';
    dropdown.appendChild(defaultOption);
    
    // Ajouter les options dynamiques basées sur les données
    allUsers.forEach(user => {
        const option = document.createElement('option');
        option.value = user.username;
        option.textContent = user.username; // Nom d'utilisateur dans le menu
        dropdown.appendChild(option);
    });

    // Ajouter un événement pour gérer la sélection
    dropdown.addEventListener('change', function(event) {
         selectedUsername = event.target.value;
        console.log('Utilisateur sélectionné:', selectedUsername);
        // Vous pouvez ajouter des actions supplémentaires ici

        if (onUserSelected) {
            onUserSelected(selectedUsername);
        }
       
        return selectedUsername;
    });

    return { dropdown, getSelectedUsername: () => selectedUsername };
}

export default createDropdownMenu;






