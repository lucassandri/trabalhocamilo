/**
 * Script personalizado para o RPG Market
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializa tooltips do Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Inicializa popovers do Bootstrap
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Inicializa dropdowns do Bootstrap de forma mais direta
    const userDropdown = document.getElementById('userDropdown');
    if (userDropdown) {
        // Remover event listeners existentes para evitar conflitos
        userDropdown.removeEventListener('click', userDropdownClickHandler);
        
        // Criar uma nova instância de dropdown
        const dropdown = new bootstrap.Dropdown(userDropdown);
        
        // Adicionar listener de evento personalizado
        userDropdown.addEventListener('click', userDropdownClickHandler);
        
        function userDropdownClickHandler(e) {
            // Previne navegação para '#' que poderia fazer a página rolar para o topo
            e.preventDefault();
            // Alterna o estado do dropdown
            dropdown.toggle();
        }
        
        // Listener para foco, melhorando a acessibilidade
        userDropdown.addEventListener('focus', function() {
            dropdown.show();
        });
    }
    
    // Funções específicas para determinadas páginas
    if (document.getElementById('countdown')) {
        updateAuctionCountdown();
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