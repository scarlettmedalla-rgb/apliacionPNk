<?php
// =========================================================================
// Rúbrica Punto 2: Guardar nueva contraseña y reactivar cuenta
// Actualiza la contraseña y reactiva la cuenta (is_blocked = 0)
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$email = strtolower(trim((string)($data['email'] ?? $data['correo'] ?? $data['username'] ?? $_POST['email'] ?? $_POST['correo'] ?? '')));
$new_password = isset($data['new_password']) ? trim((string)$data['new_password']) : '';

if (empty($email) || empty($new_password)) {
    echo json_encode([
        "status" => "error",
        "message" => "Debe ingresar el correo y la nueva contraseña."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Actualizar contraseña y reactivar cuenta (is_blocked = 0)
$stmt = $conn->prepare("UPDATE users SET password = ?, is_blocked = 0, recovery_code = NULL, recovery_code_expires = NULL WHERE LOWER(email) = ? OR LOWER(username) = ?");
$stmt->bind_param("sss", $new_password, $email, $email);
$stmt->execute();

if ($stmt->affected_rows > 0 || $stmt->errno === 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Contraseña actualizada exitosamente. Su cuenta ha sido reactivada."
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "No se pudo actualizar la contraseña. Verifique el correo ingresado."
    ]);
}

$stmt->close();
$conn->close();
?>
