<?php
// =========================================================================
// Rúbrica Punto 2: Validación de código de recuperación
// Rechaza códigos incorrectos o vencidos (después de 1 minuto)
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$email = strtolower(trim((string)($data['email'] ?? $data['correo'] ?? $data['username'] ?? $_POST['email'] ?? $_POST['correo'] ?? '')));
$code = trim((string)($data['code'] ?? $data['codigo'] ?? $_POST['code'] ?? $_POST['codigo'] ?? ''));

if (empty($email) || empty($code)) {
    echo json_encode([
        "status" => "error",
        "message" => "Debe ingresar el correo y el código de verificación."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Consultar usuario por email (insensible a mayúsculas y sin espacios accidentales)
$stmt = $conn->prepare("SELECT id, recovery_code, recovery_code_expires FROM users WHERE LOWER(email) = ? OR LOWER(username) = ? LIMIT 1");
$stmt->bind_param("ss", $email, $email);
$stmt->execute();
$res = $stmt->get_result();

if (!($user = $res->fetch_assoc())) {
    echo json_encode([
        "status" => "error",
        "message" => "Usuario no encontrado."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// 1. Manejo robusto de expiración mediante marcas de tiempo absolutas (UTC Timestamp)
// Previene fallos por discrepancias de Timezone entre PHP, MySQL o el sistema operativo
$nowUtc = time();
$expiresUtc = !empty($user['recovery_code_expires']) ? strtotime($user['recovery_code_expires']) : 0;

if ($expiresUtc === 0 || $nowUtc > $expiresUtc) {
    echo json_encode([
        "status" => "expired",
        "message" => "El código de verificación ha vencido (superó 60 segundos). Debe solicitar un nuevo código."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// 2. Coerción estricta de tipos (Type Mismatch prevention)
// Se forza a string y se eliminan posibles espacios en blanco o saltos de línea
$codigo_bd = trim((string)($user['recovery_code'] ?? ''));
$codigo_enviado = trim((string)$code);

if ($codigo_bd === '' || $codigo_bd !== $codigo_enviado) {
    echo json_encode([
        "status" => "error",
        "message" => "Código de seguridad incorrecto. Intente nuevamente."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Código válido dentro del minuto
echo json_encode([
    "status" => "success",
    "message" => "Código verificado correctamente."
]);

$stmt->close();
$conn->close();
?>
