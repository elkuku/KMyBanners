# Medallas y lista de completados

## Tu lista de completados {#tu-lista-de-completados}

La página encuentra tus banners comparando tus medallas con las imágenes de los banners de tu lista de
completados de Bannergress. Así que la app necesita esa lista primero:

1. En la página principal, toca **Iniciar sesión en Bannergress**. El inicio de sesión de Bannergress se
   abre en la app; entra como lo haces en bannergress.com (por ejemplo con Google).
2. Toca **Subir mi lista de completados**.

La lista va al repositorio de tu página (`done.json`) y la página se vuelve a crear. Cuando hayas marcado
nuevos banners como completados en Bannergress, toca **Volver a subir**.

## Haz las capturas {#haz-las-capturas}

<img class="shot-phone right" src="/screenshots/app/es/howto.webp" alt="Cómo hacer las capturas, en la app">

1. En Ingress, abre tu perfil de agente, desplázate hasta las medallas de misión y toca la flecha junto
   a ellas. Se abre la lista de todas tus medallas de misión.
2. Haz una captura. Desplázate **media pantalla**, espera a que carguen las imágenes y haz la siguiente.
   Sigue así hasta el final de la lista.
3. En tu galería, selecciona todas las capturas y compártelas con **KMyBanners**.

La app muestra cuántas capturas compartiste. Toca **Subir y crear mi página**.

<div class="shots">
  <img src="/screenshots/app/es/start-shared.webp" alt="Capturas compartidas, listas para subir">
  <img src="/screenshots/app/es/start-building.webp" alt="Se está creando la página">
</div>

Consejos:

- **Solapamiento:** cada captura tiene que mostrar parte de la anterior, para que la app pueda unirlas.
  Media pantalla es seguro.
- **Espera a las imágenes:** Ingress carga la imagen de una medalla solo cuando está en la pantalla. Un
  círculo vacío (un anillo blanco alrededor de negro) significa que aún no ha cargado: espera un momento
  antes de la captura.
- **Para actualizar bastan las medallas nuevas:** cuando tengas medallas nuevas, haz capturas desde el
  principio de la lista hasta medallas que ya están en tu página, y unas filas más. La app las une a las
  medallas de tu página donde coinciden. Hasta el final de la lista también vale.

## La creación de la página {#la-creacion}

<div style="clear: both"></div>

Después de subirlas, GitHub crea tu página: une las capturas en una imagen, la corta en filas de medallas
y compara cada fila con tu lista de completados. Tarda unos minutos; la app lo muestra y te avisa cuando
termina, también si la cierras mientras tanto.

Si falla, la app lo dice. **Ver en GitHub** abre el registro; **Reintentar** lo vuelve a empezar. Las
causas más comunes, en el registro en inglés:

| Mensaje | Qué hacer |
|---|---|
| … *do not overlap* … | Las capturas no se solapan: desplázate menos entre capturas y compártelas en orden. |
| … *had not loaded yet* … | Las medallas aún no habían cargado: espera a que carguen todas las imágenes antes de cada captura. |

## Tu página {#tu-pagina}

**Abrir mi página** la muestra. Tu página tiene:

- tus medallas de misión como en Ingress, las más nuevas primero;
- un mapa de dónde empieza cada banner, y los países;
- cada banner con sus medallas, misiones, lugar y un enlace a Bannergress, con una búsqueda y filtros
  (*Done*, *Done list only*, *To check*, *Single missions*). La página está en inglés.

<img class="shot-web" src="/screenshots/web/page.webp" alt="Una página de banners">

**Volver a crear mi página** la crea sin capturas nuevas, por ejemplo después de un cambio en
Bannergress. **GitHub** abre el repositorio.
