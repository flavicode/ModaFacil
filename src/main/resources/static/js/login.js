document.addEventListener('DOMContentLoaded', () => {
    const btnTogglePassword = document.getElementById('btnTogglePassword');
    const iconEye = document.getElementById('iconEye');
    const formularioLogin = document.getElementById('formulario-login');

    if (btnTogglePassword && formularioLogin) {
        btnTogglePassword.addEventListener('click', () => {
            const inputPassword = formularioLogin.querySelector('input[name="password"]');
            if (inputPassword) {
                if (inputPassword.type === 'password') {
                    inputPassword.type = 'text';
                    iconEye.classList.replace('bi-eye', 'bi-eye-slash');
                } else {
                    inputPassword.type = 'password';
                    iconEye.classList.replace('bi-eye-slash', 'bi-eye');
                }
            }
        });
    }
});
