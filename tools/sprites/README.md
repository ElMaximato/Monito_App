# Herramienta de animaciones

Convierte los zips de frames (`frame_001.png`, …) en hojas de sprites para el reloj y regenera
`SpriteAnimations.kt`. Se corre cada vez que llegue una animación nueva.

```bat
pip install pillow numpy scipy
python tools\sprites\build_sheets.py --src "C:\ruta\a\los\zips"
```

1. Copia el zip nuevo a esa carpeta y agrega su bloque en `animations.json`:
   `state` (código de `PetState`), `name` (archivo `anim_<name>.png`), `zip`, `fps`,
   `intro` (cuadros que se reproducen una vez, opcional) y `loop` (cuadros que se repiten).
   `from`/`to` son números de frame; `step` salta cuadros (útil para acortar introducciones largas).
2. Opcionales: `measureFrame` (cuadro neutro para medir el tamaño si hay efectos que tapan al monito)
   y `scaleAdjust` (corrección manual del tamaño).
3. `variants` reutiliza una animación con otra velocidad (RUN usa la de WALK a más fps).
4. Revisa `tools/sprites/preview/preview.gif` antes de compilar.

Salida: `android/wear/src/main/res/drawable-nodpi/anim_*.png` y
`.../presentation/pet/animation/SpriteAnimations.kt` (no se edita a mano).
