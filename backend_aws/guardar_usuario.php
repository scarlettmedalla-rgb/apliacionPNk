<?php
// =========================================================================
// Rúbrica Punto 3: Registro de usuarios con MySQLi
// Impide duplicados de nombre de usuario y correo, aplica validaciones y confirma el registro
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$username = isset($data['username']) ? trim($data['username']) : '';
$rut = isset($data['rut']) ? trim($data['rut']) : '';
$firstname = isset($data['firstname']) ? trim($data['firstname']) : '';
$lastname = isset($data['lastname']) ? trim($data['lastname']) : '';
$email = isset($data['email']) ? trim($data['email']) : '';
$phone = isset($data['phone']) ? trim($data['phone']) : '';
$password = isset($data['password']) && !empty($data['password']) ? trim($data['password']) : 'Clave123!';
$role = isset($data['role']) ? trim($data['role']) : 'Usuario';
$profile_photo = isset($data['profile_photo']) ? trim($data['profile_photo']) : null;

// Si username viene vacío, generar uno por defecto basado en firstname.lastname
if (empty($username)) {
    $clean_fn = strtolower(preg_replace('/[^a-zA-Z0-9]/', '', $firstname));
    $clean_ln = strtolower(preg_replace('/[^a-zA-Z0-9]/', '', $lastname));
    $username = $clean_fn . '.' . $clean_ln;
}

// 1. Validaciones de campos obligatorios
if (empty($rut) || empty($firstname) || empty($lastname) || empty($email)) {
    echo json_encode([
        "status" => "error",
        "message" => "Todos los campos principales (RUT, Nombres, Apellidos, Correo) son obligatorios."
    ]);
    exit;
}

// 2. Validación de formato de correo
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => "error",
        "message" => "El formato del correo electrónico es inválido."
    ]);
    exit;
}

// 3. Validación de nombres (solo letras)
if (!preg_match("/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/u", $firstname) || !preg_match("/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/u", $lastname)) {
    echo json_encode([
        "status" => "error",
        "message" => "Nombres y apellidos solo deben contener letras sin números ni símbolos."
    ]);
    exit;
}

try {
    // 4. Comprobar duplicado de correo electrónico
    $checkEmail = $conn->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
    $checkEmail->bind_param("s", $email);
    $checkEmail->execute();
    if ($checkEmail->get_result()->fetch_assoc()) {
        echo json_encode([
            "status" => "error",
            "message" => "El correo electrónico ya se encuentra registrado."
        ]);
        exit;
    }
    $checkEmail->close();

    // 5. Comprobar duplicado de nombre de usuario
    $checkUser = $conn->prepare("SELECT id FROM users WHERE username = ? LIMIT 1");
    $checkUser->bind_param("s", $username);
    $checkUser->execute();
    if ($checkUser->get_result()->fetch_assoc()) {
        echo json_encode([
            "status" => "error",
            "message" => "El nombre de usuario ($username) ya existe. Elija otro."
        ]);
        exit;
    }
    $checkUser->close();

    // 6. Inserción con sentencia preparada MySQLi
    $insert = $conn->prepare("INSERT INTO users (username, rut, firstname, lastname, email, phone, password, role, profile_photo, is_blocked) 
                             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)");
    $insert->bind_param("sssssssss", $username, $rut, $firstname, $lastname, $email, $phone, $password, $role, $profile_photo);
    $insert->execute();

    $newId = $insert->insert_id;
    $insert->close();

    echo json_encode([
        "status" => "success",
        "message" => "Usuario registrado exitosamente en MySQL.",
        "id" => (int)$newId,
        "username" => $username
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al registrar usuario: " . $e->getMessage()
    ]);
}

$conn->close();
?>
