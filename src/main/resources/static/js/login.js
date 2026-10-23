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

    // Validación registro
    const formularioRegistro = document.getElementById('formulario-registro');
    formularioRegistro.addEventListener('submit', function(event) {
        event.preventDefault();
        event.stopPropagation();

        const nombreInput = document.getElementById('reg-nombre');
        const correoInput = document.getElementById('reg-correo');
        const passwordInput = document.getElementById('reg-password');

        const errorNombre = document.getElementById('error-nombre');
        const errorCorreo = document.getElementById('error-correo');
        const errorPassword = document.getElementById('error-password');

        let nombre = nombreInput.value.trim();
        let correo = correoInput.value.trim();
        let password = passwordInput.value.trim();

        errorNombre.classList.add('d-none');
        errorCorreo.classList.add('d-none');
        errorPassword.classList.add('d-none');

        let formularioValido = true;

        // Validar Nombre
        const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(\s[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$/;
        if (!regexNombre.test(nombre)) {
            errorNombre.classList.remove('d-none');
            formularioValido = false;
        } else {
            nombre = nombre.toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
        }

        // Validar Contraseña (8 caracteres, 1 mayúscula, 1 minúscula, 1 número, 1 símbolo)
        const regexPassword = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&._\-]).{8,}$/;
        if (!regexPassword.test(password)) {
            errorPassword.classList.remove('d-none');
            formularioValido = false;
        }

        // Validar Correo
        const regexCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!regexCorreo.test(correo)) {
            errorCorreo.textContent = "Ingresa un correo electrónico válido.";
            errorCorreo.classList.remove('d-none');
            formularioValido = false;
            return;
        }
        
        fetch(`/registro/verificar-correo?correo=${encodeURIComponent(correo)}`)
            .then(response => response.json())
            .then(correoExiste => {
                if (correoExiste) {
                    errorCorreo.textContent = "El correo electrónico ya se encuentra registrado. Intenta con otro.";
                    errorCorreo.classList.remove('d-none');
                    formularioValido = false;
                }

                if (formularioValido) {
                    nombreInput.value = nombre;
                    correoInput.value = correo;
                    passwordInput.value = password;
                    formularioRegistro.submit();
                }
            });
    });
});
