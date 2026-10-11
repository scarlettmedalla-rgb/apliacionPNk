<?php
require_once 'conexion.php';

try {
    $result = $conn->query("SELECT id, name, location, temperature, humidity, date, is_thermometer_on FROM sensors ORDER BY id ASC");
    $sensors = [];
    while ($row = $result->fetch_assoc()) {
        $sensors[] = $row;
    }

    echo json_encode([
        "status" => "success",
        "data" => $sensors
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al obtener sensores: " . $e->getMessage()
    ]);
}

$conn->close();
?>
