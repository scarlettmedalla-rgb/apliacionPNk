<?php
// =========================================================================
// Rúbrica Punto 4: Eliminación de usuario con MySQLi
// =========================================================================
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$id = isset($data['id']) ? (int)$data['id'] : (isset($_GET['id']) ? (int)$_GET['id'] : 0);

if ($id <= 0) {
    echo json_encode([
        "status" => "error",
        "message" => "ID de usuario inválido."
    ]);
    exit;
}

try {
    $stmt = $conn->prepare("DELETE FROM users WHERE id = ?");
    $stmt->bind_param("i", $id);
    $stmt->execute();

    if ($stmt->affected_rows > 0) {
        echo json_encode([
            "status" => "success",
            "message" => "Usuario eliminado permanentemente de la base de datos."
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "No se encontró el usuario a eliminar."
        ]);
    }
    $stmt->close();

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al eliminar usuario: " . $e->getMessage()
    ]);
}

$conn->close();
?>
