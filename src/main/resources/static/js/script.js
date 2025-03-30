/**
 * Script personalizado para o RPG Market
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializa tooltips do Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Inicializa popovers do Bootstrap
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.forEach(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Initialize all dropdowns using Bootstrap's native approach
    const dropdownElementList = document.querySelectorAll('.dropdown-toggle');
    dropdownElementList.forEach(function(dropdownToggleEl) {
        new bootstrap.Dropdown(dropdownToggleEl);
    });
    
    // Funções específicas para determinadas páginas
    if (document.getElementById('countdown')) {
        updateAuctionCountdown();
    }
    
    // Initialize all countdowns in inventory
    const inventoryCountdowns = document.querySelectorAll('.countdown');
    console.log('Found ' + inventoryCountdowns.length + ' countdown elements');
    
    if (inventoryCountdowns.length > 0) {
        updateAllCountdowns();
        // Update countdowns every second instead of every minute
        setInterval(updateAllCountdowns, 1000);
    }

    // For form submission - prevent double submission
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Disable submit button on click to prevent double submission
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
            }
        });
    });

    // Password change form validation
    const passwordForm = document.querySelector('form[action="/aventureiro/senha"]');
    if (passwordForm) {
        const newPassword = document.getElementById('newPassword');
        const confirmPassword = document.getElementById('confirmPassword');
        
        passwordForm.addEventListener('submit', function(e) {
            let isValid = true;
            
            // Check password length
            if (newPassword.value.length < 6) {
                showValidationError(newPassword, 'A senha deve ter pelo menos 6 caracteres');
                isValid = false;
            } else {
                clearValidationError(newPassword);
            }
            
            // Check password match
            if (newPassword.value !== confirmPassword.value) {
                showValidationError(confirmPassword, 'As senhas não coincidem');
                isValid = false;
            } else {
                clearValidationError(confirmPassword);
            }
            
            if (!isValid) {
                e.preventDefault(); // Prevent form submission
            }
        });
    }
});

/**
 * Atualiza o contador regressivo para o término do leilão
 */
function updateAuctionCountdown() {
    const endDateElement = document.getElementById('endDate');
    if (!endDateElement) return;
    
    const endDate = new Date(endDateElement.value);
    const countdownElement = document.getElementById('countdown');
    
    function update() {
        const now = new Date();
        const diff = endDate - now;
        
        if (diff <= 0) {
            countdownElement.textContent = "Leilão Encerrado";
            return;
        }
        
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        
        let displayText = "";
        if(days > 0) displayText += days + "d ";
        displayText += hours + "h " + minutes + "m " + seconds + "s";
        
        countdownElement.textContent = displayText;
    }
    
    // Atualizar agora e a cada segundo
    update();
    setInterval(update, 1000);
}

/**
 * Updates all countdowns on inventory pages
 */
function updateAllCountdowns() {
    const countdowns = document.querySelectorAll('.countdown');
    
    countdowns.forEach(element => {
        try {
            const dateStr = element.getAttribute('data-end-date');
            if (!dateStr) {
                console.error('Missing data-end-date attribute on countdown element');
                return;
            }

            const endDate = new Date(dateStr);
            if (isNaN(endDate)) {
                console.error('Invalid date format in data-end-date:', dateStr);
                return;
            }
            
            const now = new Date();
            const diff = endDate - now;
            
            if (diff <= 0) {
                element.textContent = "Encerrado";
                element.classList.add('text-danger');
                return;
            }
            
            const days = Math.floor(diff / (1000 * 60 * 60 * 24));
            const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((diff % (1000 * 60)) / 1000);
            
            let displayText = "";
            if(days > 0) displayText += days + "d ";
            displayText += hours + "h " + minutes + "m " + seconds + "s";
            
            element.textContent = displayText;
        } catch (e) {
            console.error('Error updating countdown:', e);
        }
    });
}

/**
 * Show validation error for form inputs
 */
function showValidationError(inputElement, message) {
    // Clear any existing error
    clearValidationError(inputElement);
    
    // Add is-invalid class to the input
    inputElement.classList.add('is-invalid');
    
    // Create and append error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'invalid-feedback';
    errorDiv.textContent = message;
    inputElement.parentNode.appendChild(errorDiv);
}

/**
 * Clear validation error for form inputs
 */
function clearValidationError(inputElement) {
    inputElement.classList.remove('is-invalid');
    const existingError = inputElement.parentNode.querySelector('.invalid-feedback');
    if (existingError) {
        existingError.remove();
    }
}