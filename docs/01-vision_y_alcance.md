# Visión y alcance

## 1\. Contexto y motivación

**Rockalendar** nace como respuesta a una situación muy concreta y bastante común: **descubrir conciertos y festivales es difícil y caótico**, dicho de otro modo: es una mierda. La información está dispersa entre redes sociales, carteles en garitos, grupos de WhatsApp y plataformas genéricas que no están pensadas para eventos pequeños, locales o de nicho (estilos musicales alternativos).

Para el usuario medio esto se traduce en perder eventos, enterarse tarde o no poder planificar con antelación. Para quien organiza, significa poca visibilidad y dependencia excesiva de redes sociales y algoritmos.

*Rockalendar* pretende ser una **herramienta fácil y usable, centrada en conciertos y festivales**, no en feeds, likes o compromiso artificial (publicidad de pago).

## 2\. Objetivo de la aplicación

### Objetivo principal

Proporcionar una **plataforma sencilla y fiable para descubrir, crear y seguir conciertos y festivales**, con foco en la utilidad real para el usuario.

En el futuro quizá se plantee la posibilidad de abrirlo a otro tipo de eventos.

### Qué resuelve

- La dispersión de la información sobre eventos.
- La dependencia de redes sociales para enterarse de planes.
- La falta de herramientas ligeras para organizadores pequeños.
- La dificultad para planificar qué hacer en función de intereses, fechas y localización.

### Para quién

- **Usuarios finales** que quieren saber qué eventos hay cerca o en fechas concretas, sin ruido.
- **Organizadores pequeños o independientes** que quieren publicar eventos sin montar una infraestructura compleja.
- **Escenas locales o comunidades** (música, cultura alternativa, etc.) que necesitan un punto común.

No se diseña pensando en macroeventos ni en grandes promotoras, aunque no se excluyen explícitamente.

## 3\. Qué NO es (fuera de alcance / anti-objetivos)

Definir lo que *Rockalendar* **no pretende ser** es clave para evitar crecimiento descontrolado.

*Rockalendar* NO es:

- Una red social.
- Un feed infinito de contenido.
- Una plataforma de venta de entradas.
- Un sustituto de Instagram, Facebook Events o Meetup.
- Un gestor de calendarios personales tipo Google Calendar.
- Una herramienta de marketing avanzada para promotores grandes.
- Un sistema de mensajería o chat en tiempo real.

Decisiones explícitas:

- No habrá algoritmos de recomendación opacos.
- No se prioriza el engagement por encima de la utilidad.
- No se persigue "retener" al usuario artificialmente.

## 4\. Alcance funcional inicial (MVP conceptual)

En su núcleo, *Rockalendar* debe permitir:

- Crear eventos con información clara y estructurada.
- Consultar eventos por:
  - Fecha / rango de fechas
  - Ubicación (ciudad y provincia)
  - Artista o grupo
  - Texto libre (búsqueda tolerante)
- Marcar eventos como:
  - Me interesa
  - Asistiré
- Gestionar una agenda personal básica.
- Sistema de moderación:
  - Cualquier usuario registrado puede crear eventos
  - Todos los eventos se crean en estado PENDIENTE
  - Los administradores y moderadores aceptan o rechazan dichos eventos

Todo lo que no contribuya directamente a estos flujos **no forma parte del alcance inicial**.

## 5\. Métricas de éxito

El proyecto no está orientado inicialmente a monetización o crecimiento masivo, se definen métricas simples para evaluar si la aplicación cumple su propósito.

### Métricas cuantitativas

- Número de eventos creados.
  - Al menos el 50% de usuarios registrados crea un evento.
- Ratio de eventos con al menos una interacción (me interesa / asistiré).
  - Usuarios recurrentes.
  - Al menos el 50% de usuarios registrados vuelven a consultar eventos.
- Economía de tiempo y esfuerzo.
  - El tiempo medio para encontrar un evento es menos de 60 segundos.
  - Las búsquedas devuelven resultados relevantes al primer intento en el 80% de los casos.

### Métricas cualitativas

- Sensación de claridad y simplicidad.
- Facilidad para encontrar eventos sin conocer previamente el nombre exacto.
- Confianza en que la información esté actualizada.

### Métricas técnicas

- El sistema soporta 1.000 eventos sin problema (sobrecarga, lentitud, consumo excesivo de recursos, etc.).

Si *Rockalendar* se siente "útil" incluso con pocos usuarios, se considera un éxito.

## 6\. Suposiciones y restricciones

### Suposiciones

- El usuario medio escribe rápido, desde móvil y con errores.
- El interés principal es consultar, no interactuar socialmente.
- El volumen de datos inicial será bajo.
- El crecimiento (si ocurre) será progresivo, no explosivo.

### Restricciones técnicas y de producto

- Web first (responsive, no app nativa en fases iniciales).
- Backend desacoplado (API REST).
- Multi-idioma desde el diseño inicial.
- Autenticación opcional (usuarios anónimos pueden consultar).
- Infraestructura modesta y costes controlados.

No se asume disponibilidad de equipos grandes ni DevOps complejos.

## 7\. Principios de diseño del producto

Algunas ideas guía que deben servir como filtro para decisiones futuras:

- **Menos, pero mejor**: añadir funcionalidades solo si aportan valor claro.
- **Claridad sobre flexibilidad**: mejor un modelo simple que uno perfecto.
- **Transparencia**: evitar comportamientos inesperados para el usuario.
- **Evolución consciente**: no crecer por crecer.

## 8\. Horizonte y evolución

Este documento define la visión actual, no un compromiso eterno.

El alcance podrá ampliarse si: - Aparecen nuevos casos de uso reales. - Se valida el núcleo del producto. - Existen recursos para mantener la complejidad añadida.

Cualquier ampliación debería revisarse siempre contra este documento para evitar desviaciones innecesarias.

En cualquier caso, se estipula una primera versión útil (MVP para versión 1.0.0) y un Backlog de funcionalidades deseables para futuras versiones.