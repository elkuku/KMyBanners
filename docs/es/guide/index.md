# Primeros pasos

KMyBanners hace una página web con los banners de misiones de Ingress que has completado. Haces capturas
de tus medallas de misión en Ingress y las compartes con la app; ella encuentra los banners en
[Bannergress](https://bannergress.com) y publica la página.

No hay un servidor de KMyBanners: la página está en un repositorio de **tu cuenta de GitHub** y se publica
con GitHub Pages, en `https://<tu nombre de GitHub>.github.io/<nombre del repositorio>/` (`ingress-banners`
si no eliges otro nombre).
[Aquí hay una](https://elkuku.github.io/ingress-banners/).

## Lo que necesitas {#lo-que-necesitas}

- Un teléfono Android con Android 8.0 o más reciente, con Ingress.
- Una cuenta de [GitHub](https://github.com/signup). La gratuita es suficiente.
- Una cuenta de [Bannergress](https://bannergress.com) con tus banners completados marcados. La app lee
  tu lista de completados para encontrar tus banners.

## Instalar la app {#instalar-la-app}

Descarga el APK de la página de [Descargas](/es/downloads) y ábrelo en tu teléfono. Android pregunta si
tu navegador puede instalar apps: permítelo.

## Iniciar sesión con GitHub {#iniciar-sesion-con-github}

La app inicia sesión con el inicio de sesión *de dispositivo* de GitHub: muestra un código y tú lo
confirmas en GitHub en tu navegador. La app nunca ve tu contraseña de GitHub.

<div class="shots">
  <img src="/screenshots/app/es/start-signin.webp" alt="La app antes de iniciar sesión">
  <img src="/screenshots/app/es/start-code.webp" alt="La app mostrando el código">
</div>

1. Toca **Iniciar sesión con GitHub**. La app muestra un código como `CC83-AB3B`.
2. Toca **Copiar el código y abrir GitHub**. Tu navegador abre `github.com/login/device`. Si ahí no has
   iniciado sesión en GitHub, hazlo primero.
3. GitHub muestra la cuenta con la que has iniciado sesión: toca **Continue**, o **Use a different
   account**.
4. Escribe el código en las casillas y toca **Continue**. Al pegarlo puede que solo se rellene la primera
   casilla, así que escríbelo.
5. GitHub muestra lo que pide **KMyBanners**: *Public repositories* (repositorios públicos). Toca
   **Authorize**.
6. Si usas la autenticación en dos pasos, GitHub vuelve a pedir tu código (**Confirm access**).
7. GitHub dice *Congratulations, you're all set!*

<div class="shots">
  <img src="/screenshots/github/1-account.webp" alt="GitHub: Device Activation, la cuenta">
  <img src="/screenshots/github/2-code.webp" alt="GitHub: el código escrito">
  <img src="/screenshots/github/3-authorize.webp" alt="GitHub: Authorize KMyBanners">
  <img src="/screenshots/github/4-confirm.webp" alt="GitHub: Confirm access con la autenticación en dos pasos">
  <img src="/screenshots/github/5-done.webp" alt="GitHub: Congratulations, you're all set">
</div>

Luego vuelve a la app: se da cuenta sola del inicio de sesión, en unos segundos. El código vale 15
minutos; si caduca, o tocas **Cancel** en GitHub, empieza de nuevo.

La app necesita acceso a tus repositorios *públicos* para crear tu página y subir tus capturas. Solo
modifica el repositorio de tu página. El inicio de sesión de GitHub dura 8 horas y la app lo renueva
sola, así que solo inicias sesión una vez.

## Crear tu página {#crear-tu-pagina}

<img class="shot-phone right" src="/screenshots/app/es/start-create.webp" alt="Crear la página">

Escribe tu nombre de agente y, si quieres, otro nombre para el repositorio: forma parte de la dirección de
tu página, que la app muestra debajo del campo. El nombre puede tener letras, dígitos y `-` `_` `.`; por
defecto es `ingress-banners`. Toca **Crear mi página**: la app crea el repositorio público en tu cuenta y
activa GitHub Pages. Si ya tienes un repositorio con ese nombre que no es una página de banners, elige otro
nombre.

El nombre `<tu nombre de GitHub>.github.io` hace de la página el sitio propio de tu cuenta, en
`https://<tu nombre de GitHub>.github.io/`, si no lo usas ya.

<div style="clear: both"></div>

## Tu página nueva {#tu-pagina-nueva}

<img class="shot-phone right" src="/screenshots/app/es/start-empty.webp" alt="Una página nueva: aún no hay nada">

La página principal muestra tu página arriba y los pasos debajo. En una página nueva aún no hay nada:

1. [Subir tu lista de completados de Bannergress](/es/guide/medals#tu-lista-de-completados).
2. [Compartir capturas de tus medallas de misión](/es/guide/medals#haz-las-capturas).
3. [Revisar las medallas de las que la app no está segura](/es/guide/check).

Cuando los tres están hechos, se ve así, con los números de tu página arriba:

<img class="shot-phone" src="/screenshots/app/es/home.webp" alt="La página principal con una página terminada">

## Ajustes {#ajustes}

**Ajustes** (arriba a la derecha) tiene el idioma de la app (español, inglés, alemán o el del teléfono),
claro u oscuro, tu cuenta de GitHub con **Cerrar sesión**, el repositorio de tu página y la versión.

**Cambiar el nombre** cambia el nombre del repositorio, después de preguntar. Tu página se muda entonces a
la nueva dirección y se vuelve a crear allí. GitHub no redirige la dirección anterior de una página: deja
de funcionar, también en los enlaces que compartiste. La app encuentra la página por el topic `kmybanners`
que le da al repositorio, también en otro teléfono o después de cambiar el nombre en la web de GitHub.

<img class="shot-phone" src="/screenshots/app/es/settings.webp" alt="Los ajustes">
