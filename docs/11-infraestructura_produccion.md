# Infraestructura de producción — Rockalendar

> Documento de referencia para el despliegue a producción. Escrito para alguien que sabe conectarse por SSH con MobaXterm pero no tiene experiencia profunda en DevOps.

---

## Stack recomendado

```
Cloudflare (DNS + protección)
    └── Hetzner VPS CX33 (~€6/mes)
            └── Coolify (panel de gestión, gestiona Docker)
                    ├── Nuxt (frontend)
                    ├── Spring Boot (backend)
                    └── PostgreSQL (base de datos, solo accesible internamente)

Brevo (email transaccional — recuperación de contraseña, notificaciones)
Zimbra OVH (buzón ruido@rockalendar.es → reenvío a Gmail)
```

---

## Dominios

### Qué registrar
- `rockalendar.es` → dominio principal (proyecto español, escena española) ✅ registrado en OVH
- `rockalendar.com` → redirección al anterior (para que nadie lo ocupe) ✅ registrado en OVH, redirige a `rockalendar.es` con 301

> OVH incluye Zimbra Basic con los dominios pero la suscripción (~€0.30/mes) no queda clara hasta después de contratar.

### Después de registrar el dominio
Los DNS (las "instrucciones" que dicen a internet dónde está tu web) se gestionan en **Cloudflare** (gratuito), no en OVH directamente.

**¿Por qué Cloudflare si el dominio está en OVH?** Son dos cosas distintas: OVH es el registrador (el notario que te da la escritura del dominio), Cloudflare es el gestor de DNS (la centralita que decide a dónde va el tráfico). Puedes tener el dominio en OVH y delegar los DNS a Cloudflare, que es lo recomendable porque:
- Su interfaz DNS es mucho mejor que la de OVH
- Protección DDoS incluida sin coste
- Los cambios DNS se propagan en segundos en lugar de horas
- Es el sitio donde luego añadirás los registros SPF/DKIM de Brevo para el email

El proceso es:
1. Registras el dominio en OVH/IONOS
2. En Cloudflare añades el dominio y te da dos "nameservers" (ej. `ada.ns.cloudflare.com`)
3. En OVH/IONOS cambias los nameservers por los de Cloudflare
4. A partir de ahí gestionas todo desde Cloudflare

✅ Hecho. Cloudflare gestiona los DNS de `rockalendar.es`. DNSSEC activado (DS record en OVH apuntando a Cloudflare).

> **Pendiente antes de desplegar:** Cloudflare importó dos registros A (`rockalendar.es` y `www`) apuntando a OVH que hay que actualizar con la IP del VPS de Hetzner.

---

## Servidor

### Proveedor: Hetzner
https://www.hetzner.com/cloud

Empresa alemana. Datacenters en Alemania y Finlandia (dentro de la UE, cumplimiento GDPR). Mejor relación precio/potencia del mercado europeo.

### Plan recomendado: CX33
- 4 vCPU, 8 GB RAM, 80 GB SSD
- **~€6/mes** (~€72/año)
- Ubicación: Falkenstein o Núremberg (Alemania)

> El plan CX22 (~€4.5/mes) sería suficiente para solo la aplicación, pero Coolify necesita algo más de RAM. CX33 es la opción cómoda.

### Coolify — el panel que te ahorra la vida
https://coolify.io

Es un software open source que se instala en el VPS y te da una interfaz web para gestionar despliegues. Elimina la mayor parte del trabajo manual de DevOps:

- Conectas tu repositorio de GitHub
- Configuras los servicios (backend, frontend, base de datos)
- Gestiona SSL (certificados HTTPS) automáticamente con Let's Encrypt
- Despliegues automáticos cuando haces push a `main`
- Variables de entorno por interfaz web
- Logs en tiempo real

**Coolify usa Docker por debajo** — no lo sustituye. Tu `docker/compose.yml` actual es compatible.

### Instalación de Coolify
Se instala con un solo comando en el servidor (lo ejecutas desde MobaXterm una vez conectado):
```bash
curl -fsSL https://cdn.coollabs.io/coolify/install.sh | bash
```
Después de eso todo se gestiona desde el navegador.

---

## Email

### Dos necesidades distintas

#### 1. Email transaccional (el que envía el código)
Para recuperación de contraseña, notificaciones de moderación, etc.

**Servicio: Brevo** (https://www.brevo.com/es/)
- Empresa francesa, sede en la UE, GDPR nativo
- Plan gratuito: 300 emails/día (más que suficiente para beta y primeros meses)
- Los emails salen como `noreply@rockalendar.es`
- Spring Boot lo usa como cualquier servidor SMTP — solo hay que configurar unas variables de entorno

**Proceso de configuración:**
1. Crear cuenta en Brevo
2. Añadir el dominio `rockalendar.es` en Brevo — te da registros DKIM x2 y DMARC
3. Añadir esos registros en Cloudflare → DNS → Records
4. Verificar el dominio en Brevo
5. Añadir las credenciales SMTP de Brevo como variables de entorno en el backend

✅ Pasos 1-4 completados. El dominio `rockalendar.es` está verificado en Brevo.

#### 2. Buzón para leer y responder correos

✅ Configurado: `ruido@rockalendar.es` via **Zimbra Basic de OVH** (~€0.30/mes).

- Los emails que llegan a `ruido@rockalendar.es` se reenvían automáticamente a Gmail personal.
- Gmail está configurado para enviar como `ruido@rockalendar.es` usando Brevo como SMTP relay (Ajustes → Cuentas e importación → Enviar correo como).
- Al responder un email recibido en `ruido@`, Gmail selecciona automáticamente esa dirección como remitente.

`noreply@rockalendar.es` **no necesita buzón** — Brevo envía con esa dirección como "from" una vez verificado el dominio. Si alguien responde, el email rebota (comportamiento correcto para un noreply).

---

## Seguridad

### Acceso al servidor (SSH)
El servidor **no tiene contraseña**. Se accede con un **par de claves**:
- **Clave privada**: un fichero en tu ordenador (como una llave física). Sin este fichero no se puede entrar.
- **Clave pública**: se instala en el servidor. Solo sirve para verificar que tu clave privada es válida.

En MobaXterm: al crear la sesión SSH, en lugar de contraseña seleccionas "Use private key" y apuntas al fichero de tu clave privada. Funciona igual que siempre, pero es mucho más seguro.

> **Importante:** guarda la clave privada en un lugar seguro. Si la pierdes, pierdes el acceso al servidor. Puedes tener una copia de seguridad en un gestor de contraseñas (Bitwarden, 1Password).

### Base de datos
PostgreSQL corre dentro de la red interna de Docker. **No está expuesta a internet** — no tiene puerto abierto hacia fuera.

Para acceder desde DBeaver en tu ordenador usas un **túnel SSH**:
- En DBeaver, pestaña "SSH Tunnel" de la conexión
- Pones la IP del servidor y tu clave privada
- DBeaver conecta a la base de datos como si fuera local, pero todo el tráfico va cifrado por el túnel SSH

### Firewall
Solo tres puertos abiertos en el servidor:
- `80` — HTTP (Coolify lo redirige a HTTPS)
- `443` — HTTPS
- `22` — SSH (o un puerto alternativo para más seguridad)

Todo lo demás bloqueado.

### SSL / HTTPS
Coolify lo gestiona automáticamente con **Let's Encrypt** (gratuito). No hay que hacer nada manual.

---

## GDPR

| Requisito                    | Cómo se cumple                         |
|------------------------------|----------------------------------------|
| Datos en la UE               | Hetzner datacenter en Alemania         |
| Acuerdo con el procesador    | DPA disponible en Hetzner              |
| Email transaccional en la UE | Brevo (Francia)                        |
| Acceso restringido a datos   | SSH con clave privada + BD no expuesta |

---

## Costes totales estimados

| Concepto                             | Coste (primer año)        | Siguientes años | Notas                              |
|--------------------------------------|---------------------------|-----------------|------------------------------------|
| `rockalendar.es`                     | ~€6/año                   | ~€7/año         |                                    |
| `rockalendar.com`                    | ~€8/año                   | ~€13,50/año     | ~€10/año con suscripción de 5 años |
| Zimbra Basic OVH (buzón ruido@)      | ~€3.60/año (~€0.30/mes)   | Sin info        | Verificar factura el 22/04/2026    |
| Hetzner CX33                         | ~€72/año (~€6/mes)        | Sin info        |                                    |
| Cloudflare (DNS + protección básica) | €0                        | Sin info        |                                    |
| Brevo (email transaccional)          | €0 (hasta 300/día)        | Sin info        |                                    |
| SSL (Let's Encrypt vía Coolify)      | €0                        | Sin info        |                                    |
| **Total año 1**                      | **~€90/año (~€7.5/mes)**  | **+€7/año**     |                                    |

---

## Orden de pasos para salir a producción

1. ✅ **Registrar dominios** en OVH (`rockalendar.es` y `rockalendar.com`)
2. ✅ **Configurar Cloudflare**: añadir dominio, cambiar nameservers, activar DNSSEC
3. ✅ **Configurar email**: Zimbra (reenvío a Gmail) + Brevo (dominio verificado) + Gmail (enviar como ruido@)
4. ✅ **Redirección .com → .es** (301 en OVH)
5. **Decidir estrategia de imágenes** antes de tocar los registros A en Cloudflare
6. **Actualizar registros A en Cloudflare** con la IP del VPS (rockalendar.es y www)
7. **Crear VPS en Hetzner**: elegir CX33, datacenter Alemania, subir clave pública SSH
8. **Instalar Coolify** en el VPS (un comando desde MobaXterm)
9. **Configurar dominio en Coolify**: apuntar `rockalendar.es` al servidor
10. **Conectar repositorio GitHub** en Coolify
11. **Configurar servicios** (frontend, backend, PostgreSQL) en Coolify
12. **Configurar variables de entorno** de producción (JWT_SECRET, Brevo SMTP, etc.)
13. **Primer despliegue** desde Coolify
14. **Verificar SSL** (Coolify lo lanza automáticamente)
15. **Probar acceso a BD** desde DBeaver vía túnel SSH

---

## Glosario rápido

| Término           | Qué es                                                                                        |
|-------------------|-----------------------------------------------------------------------------------------------|
| **VPS**           | Servidor virtual que alquilas — una máquina en la nube que es tuya                            |
| **DNS**           | Las "páginas amarillas" de internet: traducen `rockalendar.es` a una IP                       |
| **Nameservers**   | Los servidores que guardan tu configuración DNS (los de Cloudflare en este caso)              |
| **SSL / HTTPS**   | El candado del navegador. Cifra el tráfico entre el usuario y el servidor                     |
| **Let's Encrypt** | Autoridad que emite certificados SSL gratis                                                   |
| **SSH**           | Protocolo para conectarte al servidor de forma segura (lo que usas con MobaXterm)             |
| **Túnel SSH**     | Conexión cifrada que enruta tráfico a través del servidor — así accedes a la BD desde DBeaver |
| **SPF / DKIM**    | Registros DNS que acreditan que tus emails son legítimos y no spam                            |
| **DPA**           | Acuerdo de procesamiento de datos — el contrato GDPR con Hetzner                              |
| **Docker**        | Sistema de contenedores — empaqueta la app para que funcione igual en cualquier máquina       |
| **Coolify**       | Panel web que gestiona Docker en tu servidor — tu "centro de control"                         |
