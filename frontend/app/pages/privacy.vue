<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "public" });

const { t, locale } = useI18n();
useHead({ title: () => t("page.privacy") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <button class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" :aria-label="t('common.back')" @click="$router.back()">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("page.privacy") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>

        <!-- Castellano -->
        <div v-if="locale === 'es'" class="privacy-content flex flex-column gap-4 line-height-3 text-color-secondary">
          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">1. Responsable del tratamiento</h2>
            <p class="m-0">El responsable del tratamiento de los datos personales recogidos a través de esta aplicación es Rockalendar. Puedes contactar <NuxtLink :to="ROUTES.contact" class="underline">a través del formulario</NuxtLink> o enviando un correo a <a href="mailto:admin@rockalendar.com" class="underline">admin@rockalendar.com</a></p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">2. Datos que se recogen</h2>
            <p class="m-0 mb-2">La aplicación únicamente recoge el siguiente dato personal:</p>
            <ul class="m-0 pl-4">
              <li>Dirección de correo electrónico (email)</li>
            </ul>
            <p class="m-0 mt-2">No se recogen datos adicionales de carácter personal.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">3. Finalidad del tratamiento</h2>
            <p class="m-0 mb-2">El email del usuario se utiliza exclusivamente para:</p>
            <ul class="m-0 pl-4">
              <li>Crear y gestionar la cuenta de usuario.</li>
              <li>Permitir la autenticación (inicio de sesión).</li>
              <li>Garantizar la seguridad de la cuenta (cambio y/o recuperación de contraseña).</li>
              <li>Enviar comunicaciones necesarias para el funcionamiento del servicio (por ejemplo, confirmación de cuenta o avisos relacionados con la actividad del usuario).</li>
            </ul>
            <p class="m-0 mt-2">En ningún caso se utilizará el email para el envío de publicidad, comunicaciones comerciales ni será cedido a terceros.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">4. Base legal</h2>
            <p class="m-0">La base legal para el tratamiento de los datos es la ejecución de un contrato, en el marco de la prestación del servicio ofrecido por la aplicación.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">5. Conservación de los datos</h2>
            <p class="m-0">Los datos se conservarán mientras la cuenta de usuario permanezca activa. En caso de que el usuario elimine su cuenta o solicite la supresión de sus datos, estos serán eliminados de forma definitiva de nuestros sistemas.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">6. Destinatarios de los datos</h2>
            <p class="m-0">No se cederán datos a terceros, salvo obligación legal. No obstante, podrán intervenir proveedores de servicios necesarios para el funcionamiento de la aplicación (por ejemplo, servicios de hosting o envío de correos electrónicos), que actuarán como encargados del tratamiento y cumplirán con la normativa vigente en materia de protección de datos.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">7. Derechos del usuario</h2>
            <p class="m-0 mb-2">El usuario tiene derecho a:</p>
            <ul class="m-0 pl-4">
              <li>Acceder a sus datos personales</li>
              <li>Solicitar la rectificación de datos inexactos</li>
              <li>Solicitar la supresión de sus datos</li>
              <li>Solicitar la limitación del tratamiento</li>
              <li>Oponerse al tratamiento</li>
              <li>Solicitar la portabilidad de sus datos</li>
              <li>Reclamar ante la AEPD (Agencia Española de Protección de Datos) <a href="https://www.aepd.es" target="_blank" rel="noopener noreferrer" class="underline">www.aepd.es</a></li>
            </ul>
            <p class="m-0 mt-2">Para ejercer estos derechos, el usuario podrá ponerse en contacto con el responsable del tratamiento a través de los medios habilitados en la aplicación.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">8. Seguridad de los datos</h2>
            <p class="m-0">Se han adoptado las medidas técnicas y organizativas necesarias para garantizar la seguridad de los datos personales y evitar su alteración, pérdida, tratamiento o acceso no autorizado. Las contraseñas se almacenan cifradas y nunca en texto plano.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">9. Cookies</h2>
            <p class="m-0">La aplicación utiliza cookies técnicas necesarias para gestionar la sesión del usuario y recordar sus preferencias de idioma. No se utilizan cookies analíticas, publicitarias ni de seguimiento.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">10. Modificaciones</h2>
            <p class="m-0">El responsable se reserva el derecho a modificar la presente política de privacidad para adaptarla a novedades legislativas o técnicas. En caso de cambios relevantes, se informará a los usuarios a través de la propia aplicación antes de que dichos cambios entren en vigor.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">11. Legislación aplicable</h2>
            <p class="m-0">La presente política de privacidad se rige por el Reglamento General de Protección de Datos (RGPD), la Ley Orgánica 3/2018 de Protección de Datos Personales y garantía de los derechos digitales (LOPDGDD), y demás normativa aplicable en materia de protección de datos.</p>
          </section>
        </div>

        <!-- English -->
        <div v-else class="privacy-content flex flex-column gap-4 line-height-3 text-color-secondary">
          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">1. Data Controller</h2>
            <p class="m-0">The data controller for the personal data collected through this application is Rockalendar. You can get in touch <NuxtLink :to="ROUTES.contact" class="underline">via the contact form</NuxtLink> or by sending an email to <a href="mailto:admin@rockalendar.com" class="underline">admin@rockalendar.com</a></p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">2. Data Collected</h2>
            <p class="m-0 mb-2">The application collects only the following personal data:</p>
            <ul class="m-0 pl-4">
              <li>Email address</li>
            </ul>
            <p class="m-0 mt-2">No additional personal data is collected.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">3. Purpose of Processing</h2>
            <p class="m-0 mb-2">The user's email is used exclusively to:</p>
            <ul class="m-0 pl-4">
              <li>Create and manage the user account.</li>
              <li>Enable authentication (login).</li>
              <li>Ensure account security (password change and/or recovery).</li>
              <li>Send communications necessary for the operation of the service (for example, account confirmation or notifications related to user activity).</li>
            </ul>
            <p class="m-0 mt-2">The email will never be used for advertising or commercial communications, nor will it be shared with third parties.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">4. Legal Basis</h2>
            <p class="m-0">The legal basis for data processing is the performance of a contract, in the context of providing the service offered by the application.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">5. Data Retention</h2>
            <p class="m-0">Data will be retained for as long as the user account remains active. If the user deletes their account or requests the erasure of their data, it will be permanently deleted from our systems.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">6. Data Recipients</h2>
            <p class="m-0">Data will not be shared with third parties, except where required by law. However, service providers necessary for the operation of the application (such as hosting or email delivery services) may be involved. These providers act as data processors and comply with applicable data protection regulations.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">7. User Rights</h2>
            <p class="m-0 mb-2">Users have the right to:</p>
            <ul class="m-0 pl-4">
              <li>Access their personal data</li>
              <li>Request the rectification of inaccurate data</li>
              <li>Request the erasure of their data</li>
              <li>Request the restriction of processing</li>
              <li>Object to processing</li>
              <li>Request data portability</li>
              <li>Lodge a complaint with the AEPD (Spanish Data Protection Authority) at <a href="https://www.aepd.es" target="_blank" rel="noopener noreferrer" class="underline">www.aepd.es</a></li>
            </ul>
            <p class="m-0 mt-2">To exercise these rights, users may contact the data controller through the channels available in the application.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">8. Data Security</h2>
            <p class="m-0">The necessary technical and organisational measures have been implemented to ensure the security of personal data and prevent its alteration, loss, unauthorised processing or access. Passwords are stored encrypted and never in plain text.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">9. Cookies</h2>
            <p class="m-0">The application uses technical cookies necessary to manage the user session and remember language preferences. No analytical, advertising or tracking cookies are used.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">10. Changes to This Policy</h2>
            <p class="m-0">The data controller reserves the right to modify this privacy policy to reflect legislative or technical developments. In the event of significant changes, users will be notified through the application before those changes take effect.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">11. Applicable Law</h2>
            <p class="m-0">This privacy policy is governed by the General Data Protection Regulation (GDPR), the Spanish Organic Law 3/2018 on Personal Data Protection and Digital Rights Guarantee (LOPDGDD), and any other applicable data protection legislation.</p>
          </section>
        </div>

      </template>
    </Card>
  </div>
</template>
