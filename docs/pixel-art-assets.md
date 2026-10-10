# Arte integrado y validación

Recursos PNG creados con la herramienta integrada de generación de imágenes:

- drawable-nodpi/egg_night.png: parque nocturno, ciudad, farol, vegetación y terraza; sin personajes ni luna.
- drawable-nodpi/egg_professor.png: ocho poses transparentes, cuatro columnas por dos filas.
- drawable-nodpi/egg_blast.png: cuatro fases transparentes de explosión y humo.

Dirección de arte de los prompts: escenario detallado 16-bit azul y violeta con luz cálida; profesor con gafas, barba, chaleco azul con bandas claras y pantalones oscuros; explosión arcade dorada y naranja. Sin texto incrustado: los diálogos y controles se dibujan en Android.

La escena utiliza coordenadas lógicas de 360 unidades de ancho para mantener botones y personaje legibles. El escenario pintado se desplaza suavemente; luna, estrellas, luces, personaje, proyectil y partículas se renderizan por separado. La ciudad y la vegetación todavía forman parte de una sola pintura de fondo, no de capas PNG independientes.

Las poses se seleccionan desde el atlas; caminar usa dos fotogramas, respirar usa desplazamiento suave y hablar alterna poses mediante temporizador. No hay sincronización por fonemas.

Prueba en emulador API 37: entrada, pregunta, SÍ, celebración, espera prolongada, pulsación, proyectil, explosión, oscuridad, reinicio y NO. Capturas generadas por EasterEggSceneTest. Compilación debug correcta.

El audio del usuario se mantiene en res/raw/easter_egg_audio.mp3. Los efectos breves actuales usan ToneGenerator; no se incluye aún una pista musical épica completa.
