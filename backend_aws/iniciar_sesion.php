<?php
// =========================================================================
// Rúbrica Punto 1: Inicio de sesión
// Valida credenciales en MySQL con MySQLi, verifica estado activo y rechaza cuentas bloqueadas
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$username_or_email = isset($data['username']) ? trim($data['username']) : (isset($data['email']) ? trim($data['email']) : '');
$password = isset($data['password']) ? trim($data['password']) : '';

if (empty($username_or_email) || empty($password)) {
    echo json_encode([
        "status" => "error",
        "message" => "Debe ingresar el usuario/correo y la contraseña."
    ]);
    exit;
}

// Búsqueda por email o nombre de usuario
$stmt = $conn->prepare("SELECT id, username, rut, firstname, lastname, email, password, role, profile_photo, is_blocked FROM users WHERE (email = ? OR username = ?) LIMIT 1");
$stmt->bind_param("ss", $username_or_email, $username_or_email);
$stmt->execute();
$result = $stmt->get_result();

if ($user = $result->fetch_assoc()) {
    // Verificar si la cuenta está bloqueada por seguridad
    if ((int)$user['is_blocked'] === 1) {
        echo json_encode([
            "status" => "blocked",
            "is_blocked" => "1",
            "message" => "La cuenta se encuentra bloqueada por seguridad. Debe recuperar su contraseña o contactar a soporte."
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Validar contraseña
    if ($user['password'] === $password) {

        // Acceso exitoso
        unset($user['password']); // No exponer el hash o clave en la respuesta
        echo json_encode([
            "status" => "success",
            "message" => "Inicio de sesión correcto.",
            "data" => $user
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

// Credenciales incorrectas
echo json_encode([
    "status" => "error",
    "message" => "Credenciales incorrectas. Verifique su usuario y contraseña."
]);
$stmt->close();
$conn->close();
?>
