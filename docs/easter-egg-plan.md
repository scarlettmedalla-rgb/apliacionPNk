# Easter egg: plan de implementación

## Escenas y estados

1. Entrada: profesor camina al centro.
2. Pregunta: noche con cielo, luna, estrellas y ciudad en parallax; «¿Se sacaron un 7?» y SÍ/NO.
3. NO: reacción «PENKAAAA», MP3 del usuario, «Entonces no hay animación», gag, salida y reinicio.
4. SÍ: «¡BUENAAA!», celebración, «Okey, entonces sí hay animación» y giro.
5. Espera: botón rojo «¡¡APRETAR!!» hasta pulsación.
6. Disparo: bazuca ficticia, luna, destello, humo, partículas y vibración opcional.
7. Oscuridad: pantalla negra con ojos, MP3 y reinicio.

## Componentes y reemplazos

- `EasterEggView`: máquina de estados, renderizado 16-bit, parallax, sprites procedurales y partículas.
- `EasterEggActivity`: ciclo de vida seguro de `MediaPlayer` y vibración opcional.
- `btn_easter_egg`: acceso «¿?» desde registro.
- `res/raw/easter_egg_audio.mp3`: audio del usuario.

El renderizador usa marcadores pixel-art procedurales para que el flujo sea funcional sin inventar assets externos. `drawTeacher`, `drawBazooka`, `drawCity` y `drawBlast` son puntos de reemplazo para PNG transparentes por estado.
