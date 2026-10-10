# Coreografía y sincronización — revisión 2

## Flujo

SÍ: baile frontal con balanceo lateral y brazos → mensaje → espera indefinida mirando al jugador → pulsación roja → giro → levantar bazuca → proyectil → explosión → negro → 400 ms de silencio → MP3 → reinicio.

NO: gesto del dedo medio → MP3 con boca guiada por su señal → espera de final de voz → mensaje «Entonces no hay animación» que permanece mientras sale caminando → pausa → entrada.

El video de referencia es https://www.youtube.com/shorts/zUKzVBkcHeg. El baile es una adaptación de balanceo, pasos y brazos al profesor, no una reproducción fotograma a fotograma ni utiliza música extraída del video.

## Audio

EggVoiceEnvelope decodifica el MP3 incluido con MediaCodec en un hilo propio. Calcula RMS en ventanas de 20 ms y selecciona la apertura de boca con MediaPlayer.currentPosition. Las pausas cierran la boca; no se solicita micrófono ni se analiza audio del dispositivo. Es sincronización por amplitud, no por fonemas.

## Disparo

La bazuca es una capa independiente. El ángulo se calcula desde el hombro hacia la luna; la salida del proyectil está en el extremo de esa misma dirección. El destino lunar queda fijo durante el disparo. El misil rota según ese ángulo y su humo sigue el mismo segmento.

## Recursos

Generados con la herramienta integrada de imágenes y guardados en drawable-nodpi:

- egg_motion.png: atlas 4×2 con cuatro poses de caminata, dos de baile y gesto con boca cerrada/abierta. Prompt: conservar personaje de referencia, vestimenta y escala; transparencia; ciclos de pasos y baile; gesto cómico del dedo medio.
- egg_back.png: vista posterior completa del mismo profesor, brazos preparados para sujetar arma independiente, sin rostro visible.
- egg_launcher.png: bazuca caricaturesca horizontal mirando a la derecha, oliva y dorada, sin manos ni fondo.

Se mantienen los PNG originales para las demás poses.
