'use strict';




const createDropdownMenu = async (allUsers, onUserSelected)  => {

    let selectedUsername = null;
    // Créer l'élément select pour le menu déroulant
    const dropdown = document.createElement('select');
    dropdown.id = 'dynamic-dropdown';
    
    // Créer l'option par défaut
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

    // Retourner l'élément dropdown créé
    return { dropdown, getSelectedUsername: () => selectedUsername };
}

export default createDropdownMenu;






