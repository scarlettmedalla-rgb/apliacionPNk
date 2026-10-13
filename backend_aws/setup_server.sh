#!/bin/bash
# ==============================================================================
# Script de Instalación Automatizada para AWS EC2 (Ubuntu)
# Proyecto: SmartTemp / ApliacionPNk
# Instala: Apache, PHP, MySQL, phpMyAdmin
# Configura: Usuario 'admin' / 'admin', base de datos 'pnk_iot_db', tablas y API REST
# ==============================================================================

set -e

echo ">>> 1. Actualizando paquetes del sistema..."
sudo apt-get update -y

echo ">>> 2. Instalando Apache2, PHP, MySQL Server y utilidades..."
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y \
    apache2 \
    php \
    libapache2-mod-php \
    php-mysql \
    php-mbstring \
    php-zip \
    php-gd \
    php-json \
    php-curl \
    mysql-server \
    unzip \
    wget \
    curl

echo ">>> 3. Habilitando e iniciando servicios Apache y MySQL..."
sudo systemctl enable apache2
sudo systemctl start apache2
sudo systemctl enable mysql
sudo systemctl start mysql

echo ">>> 4. Configurando usuario MySQL 'admin' con clave 'admin'..."
sudo mysql -e "CREATE USER IF NOT EXISTS 'admin'@'localhost' IDENTIFIED WITH mysql_native_password BY 'admin';"
sudo mysql -e "ALTER USER 'admin'@'localhost' IDENTIFIED WITH mysql_native_password BY 'admin';"
sudo mysql -e "GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION;"
sudo mysql -e "FLUSH PRIVILEGES;"

echo ">>> 5. Creando base de datos pnk_iot_db e importando tablas..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/database.sql" ]; then
    sudo mysql -u admin -padmin < "$SCRIPT_DIR/database.sql"
    echo "Tablas importadas correctamente desde $SCRIPT_DIR/database.sql"
elif [ -f "/var/www/html/api/database.sql" ]; then
    sudo mysql -u admin -padmin < "/var/www/html/api/database.sql"
    echo "Tablas importadas correctamente desde /var/www/html/api/database.sql"
else
    echo "Descargando database.sql desde repositorio..."
    curl -sSL https://raw.githubusercontent.com/scarlettmedalla-rgb/apliacionPNk/main/backend_aws/database.sql | sudo mysql -u admin -padmin
fi

echo ">>> 6. Instalando phpMyAdmin..."
PHPMYADMIN_VER="5.2.1"
if [ ! -d "/var/www/html/phpmyadmin" ]; then
    cd /tmp
    wget -q "https://files.phpmyadmin.net/phpMyAdmin/${PHPMYADMIN_VER}/phpMyAdmin-${PHPMYADMIN_VER}-all-languages.tar.gz" -O phpmyadmin.tar.gz
    tar -xzf phpmyadmin.tar.gz
    sudo rm -rf /var/www/html/phpmyadmin
    sudo mv "phpMyAdmin-${PHPMYADMIN_VER}-all-languages" /var/www/html/phpmyadmin
    rm phpmyadmin.tar.gz
    
    # Configuración de phpMyAdmin
    sudo cp /var/www/html/phpmyadmin/config.sample.inc.php /var/www/html/phpmyadmin/config.inc.php
    BLOWFISH_SECRET=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
    sudo sed -i "s/\$cfg\['blowfish_secret'\] = ''/\$cfg\['blowfish_secret'\] = '${BLOWFISH_SECRET}'/" /var/www/html/phpmyadmin/config.inc.php
    sudo mkdir -p /var/www/html/phpmyadmin/tmp
    sudo chown -R www-data:www-data /var/www/html/phpmyadmin/tmp
    sudo chmod 777 /var/www/html/phpmyadmin/tmp
fi

echo ">>> 7. Desplegando archivos PHP de la API en /var/www/html/api/..."
sudo mkdir -p /var/www/html/api
if [ -d "$SCRIPT_DIR" ]; then
    sudo cp "$SCRIPT_DIR"/*.php /var/www/html/api/ 2>/dev/null || true
fi
sudo chown -R www-data:www-data /var/www/html/

echo ">>> 8. Reiniciando Apache..."
sudo systemctl restart apache2

echo "=================================================================="
echo ">>> ¡INSTALACIÓN COMPLETADA CON ÉXITO!"
echo ">>> phpMyAdmin disponible en: http://$(curl -s ifconfig.me)/phpmyadmin"
echo ">>> Usuario: admin | Clave: admin"
echo ">>> API disponible en:       http://$(curl -s ifconfig.me)/api/"
echo "=================================================================="
