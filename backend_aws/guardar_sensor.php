<?php
require_once 'conexion.php';

$raw_input = file_get_contents('php://input');
$data = json_decode($raw_input, true);
if (!$data) {
    $data = $_POST;
}

$name = isset($data['name']) ? trim($data['name']) : '';
$location = isset($data['location']) ? trim($data['location']) : '';
$temperature = isset($data['temperature']) ? trim($data['temperature']) : '';
$humidity = isset($data['humidity']) ? trim($data['humidity']) : '';
$date = isset($data['date']) && !empty($data['date']) ? trim($data['date']) : date('d/m/Y - H:i:s');
$is_thermometer_on = isset($data['is_thermometer_on']) ? (int)$data['is_thermometer_on'] : 1;

if (empty($name) || empty($location) || empty($temperature)) {
    echo json_encode([
        "status" => "error",
        "message" => "Faltan campos obligatorios (name, location, temperature)."
    ]);
    exit;
}

try {
    $stmt = $conn->prepare("INSERT INTO sensors (name, location, temperature, humidity, date, is_thermometer_on) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("sssssi", $name, $location, $temperature, $humidity, $date, $is_thermometer_on);
    $stmt->execute();

    $newId = $stmt->insert_id;
    $stmt->close();

    echo json_encode([
        "status" => "success",
        "message" => "Sensor registrado exitosamente en MySQL.",
        "id" => (int)$newId
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Error al guardar sensor: " . $e->getMessage()
    ]);
}

$conn->close();
?>
