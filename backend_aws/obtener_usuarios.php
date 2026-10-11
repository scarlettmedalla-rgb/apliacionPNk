<?php
// =========================================================================
// Rúbrica Punto 4: Consulta de usuarios desde MySQL usando MySQLi
// Filtra por nombres y apellidos sin distinguir mayúsculas ni acentos (collation utf8mb4_unicode_ci)
// =========================================================================
require_once 'conexion.php';

$search = isset($_GET['search']) ? trim($_GET['search']) : '';

try {
    if (!empty($search)) {
        // En MySQL con utf8mb4_unicode_ci la búsqueda LIKE no distingue mayúsculas ni acentos
        $query = "%" . $search . "%";
        $stmt = $conn->prepare("SELECT id, username, rut, firstname, lastname, email, phone, role, profile_photo, is_blocked 
                                FROM users 
                                WHERE firstname LIKE ? OR lastname LIKE ? OR CONCAT(firstname, ' ', lastname) LIKE ?
                                ORDER BY id ASC");
        $stmt->bind_param("sss", $query, $query, $query);
        $stmt->execute();
        $result = $stmt->get_result();
    } else {
        $result = $conn->query("SELECT id, username, rut, firstname, lastname, email, phone, role, profile_photo, is_blocked FROM users ORDER BY id ASC");
    }

    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }

    echo json_encode([
        "status" => "success",
        "data" => $users
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al consultar usuarios: " . $e->getMessage()
    ]);
}

$conn->close();
?>
