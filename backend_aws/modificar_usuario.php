<?php
// =========================================================================
// Rúbrica Punto 4: Modificación de usuario con MySQLi
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$id = isset($data['id']) ? (int)$data['id'] : 0;
$firstname = isset($data['firstname']) ? trim($data['firstname']) : '';
$lastname = isset($data['lastname']) ? trim($data['lastname']) : '';
$email = isset($data['email']) ? trim($data['email']) : '';
$profile_photo = isset($data['profile_photo']) ? trim($data['profile_photo']) : null;

if ($id <= 0 || empty($firstname) || empty($lastname) || empty($email)) {
    echo json_encode([
        "status" => "error",
        "message" => "Faltan campos obligatorios para actualizar el usuario."
    ]);
    exit;
}

// Validaciones
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => "error",
        "message" => "Correo electrónico no válido."
    ]);
    exit;
}

if (!preg_match("/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/u", $firstname) || !preg_match("/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/u", $lastname)) {
    echo json_encode([
        "status" => "error",
        "message" => "Nombres y apellidos solo deben contener letras sin números."
    ]);
    exit;
}

try {
    // Comprobar que el correo no esté ocupado por OTRO usuario
    $check = $conn->prepare("SELECT id FROM users WHERE email = ? AND id != ? LIMIT 1");
    $check->bind_param("si", $email, $id);
    $check->execute();
    if ($check->get_result()->fetch_assoc()) {
        echo json_encode([
            "status" => "error",
            "message" => "El correo electrónico ya pertenece a otro usuario."
        ]);
        exit;
    }
    $check->close();

    $stmt = $conn->prepare("UPDATE users SET firstname = ?, lastname = ?, email = ?, profile_photo = COALESCE(?, profile_photo) WHERE id = ?");
    $stmt->bind_param("ssssi", $firstname, $lastname, $email, $profile_photo, $id);
    $stmt->execute();

    echo json_encode([
        "status" => "success",
        "message" => "Usuario actualizado correctamente en MySQL."
    ]);
    $stmt->close();

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al modificar usuario: " . $e->getMessage()
    ]);
}

$conn->close();
?>
