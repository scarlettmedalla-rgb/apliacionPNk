-- =====================================================
-- Base de Datos MySQL para AWS (EC2 / RDS)
-- Cumple con la rúbrica completa del proyecto SmartTemp
-- =====================================================
CREATE DATABASE IF NOT EXISTS pnk_iot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pnk_iot_db;

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    rut VARCHAR(20) NOT NULL,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30) DEFAULT '',
    password VARCHAR(255) NOT NULL DEFAULT 'Clave123!',
    role VARCHAR(50) DEFAULT 'Usuario',
    profile_photo VARCHAR(255) DEFAULT NULL,
    is_blocked TINYINT(1) NOT NULL DEFAULT 0,
    recovery_code VARCHAR(10) DEFAULT NULL,
    recovery_code_expires DATETIME DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de Sensores IoT
CREATE TABLE IF NOT EXISTS sensors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(150) NOT NULL,
    temperature VARCHAR(20) NOT NULL,
    humidity VARCHAR(20) NOT NULL,
    date VARCHAR(50) DEFAULT '',
    is_thermometer_on TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Datos Iniciales de Prueba
INSERT IGNORE INTO users (id, username, rut, firstname, lastname, email, phone, password, role, profile_photo, is_blocked) VALUES
(1, 'jorge.cortes', '11.111.111-1', 'Jorge Luis', 'Cortes Gallardo', 'jorge.cortes@empresa.com', '+56911112222', 'Clave123!', 'Administrador', 'jorge_cortes', 0),
(2, 'kevin.encina', '22.222.222-2', 'Kevin', 'Encina Molina', 'kevin.encina@empresa.com', '+56933334444', 'Clave123!', 'Desarrollador', 'kevin_encina', 0),
(3, 'scarlett.williams', '33.333.333-3', 'Scarlett', 'Williams Medalla', 'scarlett.williams@empresa.com', '+56955556666', 'Clave123!', 'Administradora', 'scarlett_williams', 0),
(4, 'usuario.bloqueado', '44.444.444-4', 'Usuario', 'Bloqueado', 'bloqueado@empresa.com', '+56977778888', 'Clave123!', 'Usuario', NULL, 1);

INSERT IGNORE INTO sensors (name, location, temperature, humidity, date, is_thermometer_on) VALUES
('Sensor Clima Ovalle Central', 'Ovalle', '23.5', '58%', '10/10/2026 - 12:00:00', 1),
('Sensor Clima Coquimbo Costa', 'Coquimbo', '17.8', '74%', '10/10/2026 - 12:00:00', 1),
('Sensor La Serena Centro', 'La Serena', '19.2', '65%', '10/10/2026 - 12:00:00', 1);
