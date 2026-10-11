<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    http_response_code(200);
    exit;
}

require_once 'conexion.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

if (file_exists(__DIR__ . '/vendor/autoload.php')) {
    require_once __DIR__ . '/vendor/autoload.php';
} else {
    require_once __DIR__ . '/PHPMailer/src/Exception.php';
    require_once __DIR__ . '/PHPMailer/src/PHPMailer.php';
    require_once __DIR__ . '/PHPMailer/src/SMTP.php';
}

// Leer correo de POST o de JSON
$input = json_decode(file_get_contents('php://input'), true);
$raw_id = $_POST['email'] ?? $_POST['correo'] ?? $_POST['username'] ?? $input['email'] ?? $input['correo'] ?? $input['username'] ?? '';
$email = strtolower(trim((string)$raw_id));

if (empty($email)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "Debes indicar un correo electronico"
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Generar código criptográfico de 6 dígitos
$codigo = (string)random_int(100000, 999999);
$expiracion = date('Y-m-d H:i:s', time() + 180); // 3 minutos para evitar discrepancias de segundos

// Verificar o registrar usuario en base de datos para garantizar persistencia del código
$stmt = $conn->prepare("SELECT id, username, email FROM users WHERE LOWER(email) = ? OR LOWER(username) = ? LIMIT 1");
$stmt->bind_param("ss", $email, $email);
$stmt->execute();
$res = $stmt->get_result();

$destinatario_email = $email;

if ($user = $res->fetch_assoc()) {
    $uid = $user['id'];
    $destinatario_email = !empty($user['email']) ? $user['email'] : $email;
    $up = $conn->prepare("UPDATE users SET is_blocked = 1, recovery_code = ?, recovery_code_expires = ? WHERE id = ?");
    $up->bind_param("ssi", $codigo, $expiracion, $uid);
    $up->execute();
    $up->close();
} else {
    // Si el usuario prueba con su email personal no registrado previamente, se crea el registro temporal
    $uname = explode('@', $email)[0];
    $pass_hash = password_hash('Temp1234!', PASSWORD_DEFAULT);
    $ins = $conn->prepare("INSERT INTO users (username, rut, firstname, lastname, email, password, is_blocked, recovery_code, recovery_code_expires) VALUES (?, '11111111-1', 'Usuario', 'SmartTemp', ?, ?, 1, ?, ?)");
    $ins->bind_param("sssss", $uname, $email, $pass_hash, $codigo, $expiracion);
    $ins->execute();
    $ins->close();
    $destinatario_email = $email;
}
$stmt->close();

$mail = new PHPMailer(true);

try {
    // Configuración SMTP con puerto 465 SSL
    $mail->isSMTP();
    $mail->Host       = 'smtp.gmail.com';
    $mail->SMTPAuth   = true;
    $mail->Username   = 'scarlett.medalla@gmail.com';
    $mail->Password   = 'sckytgdsvucfekbl';
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
    $mail->Port       = 465;
    $mail->CharSet    = 'UTF-8';

    $mail->setFrom('scarlett.medalla@gmail.com', 'SmartTemp Seguridad');
    $mail->addAddress($destinatario_email);

    $mail->isHTML(true);
    $mail->Subject = "SmartTemp: Codigo de Verificacion $codigo";

    $mail->Body = '
    <div style="background-color:#f1f5f9;padding:30px 10px;font-family:Arial,sans-serif;">
      <div style="max-width:500px;margin:auto;background:#ffffff;border-radius:16px;border:1px solid #e2e8f0;overflow:hidden;text-align:center;">
        <div style="background:linear-gradient(135deg,#0f172a,#2563eb);padding:30px 20px;color:#ffffff;">
          <div style="background:#38bdf8;width:50px;height:50px;border-radius:12px;margin:auto;line-height:50px;font-size:24px;">🌡️</div>
          <h1 style="margin:10px 0 0;font-size:24px;">SmartTemp</h1>
          <p style="margin:4px 0 0;color:#93c5fd;font-size:12px;text-transform:uppercase;letter-spacing:1px;">Control Termico e IoT</p>
        </div>
        <div style="padding:30px 24px;">
          <h2 style="color:#0f172a;font-size:18px;margin-top:0;">Recuperacion de Contrasena</h2>
          <p style="color:#475569;font-size:14px;line-height:1.5;">Hola, <strong>usuario de SmartTemp</strong>: este es tu codigo de verificacion para continuar:</p>
          <div style="background:#eff6ff;border:2px dashed #3b82f6;border-radius:12px;padding:16px;margin:20px auto;display:inline-block;min-width:200px;">
            <span style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#1d4ed8;font-family:monospace;">' . $codigo . '</span>
          </div>
          <p style="color:#2563eb;font-size:13px;font-weight:600;margin:12px 0;">Este codigo expirara en 1 minuto.</p>
          <p style="color:#94a3b8;font-size:12px;margin-top:20px;">Si no solicitaste este codigo, puedes ignorar este correo de forma segura.</p>
        </div>
        <div style="background:#f8fafc;padding:14px;border-top:1px solid #e2e8f0;color:#94a3b8;font-size:11px;">
          SmartTemp Systems &bull; Notificacion Automatica
        </div>
      </div>
    </div>';

    $mail->send();

    echo json_encode([
        "status"  => "success",
        "message" => "Codigo enviado exitosamente",
        "email"   => $destinatario_email,
        "code"    => $codigo
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "status"  => "error",
        "message" => "Error SMTP: " . $mail->ErrorInfo
    ], JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>
