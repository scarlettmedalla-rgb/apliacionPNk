# Guía de Despliegue en AWS (EC2 / RDS + PHP + MySQL) - SmartTemp

Esta carpeta contiene el backend completo que da cumplimiento a la **Rúbrica de Evaluación** mediante **PHP con MySQLi** y base de datos **MySQL en AWS**.

---

## 1. Mapeo de Archivos según la Rúbrica

| Requisito de la Rúbrica | Archivo PHP en AWS | Descripción |
| :--- | :--- | :--- |
| **1. Inicio de sesión** | `iniciar_sesion.php` | Valida credenciales contra MySQL vía MySQLi. Permite entrar a cuentas activas y rechaza incorrectas o bloqueadas (`is_blocked = 1`). |
| **2. Recuperación de contraseña** | `enviar_codigo_recuperacion.php`<br>`validar_codigo.php`<br>`restablecer_password.php` | Genera código de 4 dígitos, bloquea la cuenta, envía por PHPMailer (expira en 1 minuto). Rechaza códigos incorrectos o vencidos, permite solicitar otro, y reactiva la cuenta tras actualizar contraseña. |
| **3. Registro de usuarios** | `guardar_usuario.php` | Registra en MySQL impidiendo duplicados de `username` y `email`, manteniendo todas las validaciones de nombres y seguridad. |
| **4. CRUD Usuarios** | `obtener_usuarios.php`<br>`modificar_usuario.php`<br>`eliminar_usuario.php` | Desarrollado con **MySQLi**. Filtra por nombres y apellidos ignorando mayúsculas y acentos (`utf8mb4_unicode_ci`). Permite modificar y eliminar con SweetAlert. |
| **5. Consumo IoT** | `obtener_sensores.php`<br>`guardar_sensor.php` | Persiste y lista lecturas de sensores IoT. En Android se conecta con `www.pnk.cl/muestra_datos.php`. |
| **Conexión Base** | `conexion.php`<br>`database.sql` | Conexión con **MySQLi**, UTF-8 y CORS. Script SQL con tablas `users` y `sensors`. |

---

## 2. Instalación en Servidor AWS EC2 (Ubuntu / Amazon Linux)

1. **Instalar paquetes del servidor (Apache, PHP, MySQLi)**:
   ```bash
   sudo apt update
   sudo apt install apache2 php libapache2-mod-php php-mysql mysql-server git composer -y
   sudo systemctl enable apache2
   sudo systemctl start apache2
   ```

2. **Crear la Base de Datos**:
   ```bash
   mysql -u root -p < database.sql
   ```

3. **Instalar PHPMailer (para recuperación de contraseña)**:
   ```bash
   cd /var/www/html/api
   composer require phpmailer/phpmailer
   ```
   *(O si no usas Composer, clona el repositorio de PHPMailer en `/var/www/html/api/PHPMailer`)*.

4. **Copiar los scripts PHP a la carpeta web**:
   ```bash
   sudo mkdir -p /var/www/html/api
   sudo cp *.php /var/www/html/api/
   sudo chown -R www-data:www-data /var/www/html/api/
   ```

5. **Configurar credenciales en `conexion.php`**:
   - Cambia `$username` y `$password` según tu usuario de MySQL.
   - Si usas **AWS RDS**, cambia `$host = "localhost";` por el endpoint público de RDS (ej. `midatabase.c123.us-east-1.rds.amazonaws.com`).

6. **Security Groups en AWS (Consola Web de AWS)**:
   - Ve a **EC2 -> Security Groups -> Inbound Rules**.
   - Agrega **HTTP (puerto 80)** con origen `0.0.0.0/0`.
   - Agrega **HTTPS (puerto 443)** con origen `0.0.0.0/0`.
   - (Solo si usas RDS separado): Permite **MySQL/Aurora (puerto 3306)** desde la IP privada de tu EC2.

---

## 3. Configuración en la App Android

En tu archivo [AwsApiService.kt](file:///c:/Users/scarl/OneDrive/Escritorio/aplicacion_pnk/apliacionPNk/app/src/main/java/com/example/myapplication/network/AwsApiService.kt):
```kotlin
// Reemplaza por la IP pública de tu instancia AWS EC2 o tu dominio
AwsApiService.BASE_URL = "http://TU_IP_PUBLICA_AWS/api/"
```
