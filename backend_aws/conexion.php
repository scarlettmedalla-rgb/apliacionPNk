<?php
// Configuración de cabeceras para API REST
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Configuración de MySQL con MySQLi (Requisito de la Rúbrica)
$host = "localhost"; // Cambiar por tu endpoint de RDS o localhost en EC2
$db_name = "pnk_iot_db";
$username = "admin";  // Tu usuario de MySQL en AWS
$password = "admin";  // Tu contraseña de MySQL en AWS

// Conexión usando MySQLi
$conn = new mysqli($host, $username, $password, $db_name);

if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "Error de conexión MySQLi: " . $conn->connect_error
    ]);
    exit;
}

$conn->set_charset("utf8mb4");
?>
