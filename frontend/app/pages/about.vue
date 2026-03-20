<script setup lang="ts">
definePageMeta({ layout: "public" });

const { t, locale } = useI18n();
useHead({ title: () => t("page.about") });
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <button
        class="p-0 border-none bg-transparent cursor-pointer text-color-secondary"
        :aria-label="t('common.back')"
        @click="$router.back()">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("page.about") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <!-- Castellano -->
        <div v-if="locale === 'es'" class="flex flex-column gap-4 line-height-3 text-color-secondary">
          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">¿Qué es Rockalendar?</h2>
            <p class="m-0 mb-2">
              Una app para encontrar conciertos y festivales de rock, metal, punk y todo lo que venga con actitud.
              Bienvenidas las cumbias anti fascistas. Hecha por gente de la escena, para gente de la escena. Sin
              algoritmos que te digan qué te tiene que gustar ni patrocinios que esconden lo que no interesa.
            </p>
            <p class="m-0">
              Los propios usuarios son quienes añaden los eventos. Tú también puedes. Autogestión y colaboración.
            </p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">Por qué hay moderación</h2>
            <p class="m-0 mb-2">
              Porque no todo vale. Los eventos propuestos pasan por revisión humana antes de publicarse. No porque
              desconfiemos de ti, sino porque queremos mantener fuera ciertas cosas:
            </p>
            <ul class="m-0 pl-4 mb-2">
              <li>
                Festivales financiados por promotoras con prácticas poco éticas (explotación laboral, vínculos con
                fondos de inversión genocidas).
              </li>
              <li>Festivales mainstream: ya tienen publicidad en otras plataformas.</li>
              <li>Grupos que hacen apología del fascismo, el racismo o cualquier otra forma de odio.</li>
              <li>Contenido malintencionado o directamente falso.</li>
            </ul>
            <p class="m-0">Si tu propuesta es rechazada, recibirás una explicación. Si necesita ajustes, también.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">Nuestro trato contigo</h2>
            <p class="m-0 mb-2">La cultura no es un producto. Tú tampoco.</p>
            <p class="m-0 mb-2">
              Rockalendar es y será gratuita. No hay cuentas premium, no hay eventos destacados por pasta, no hay dark
              patterns diseñados para tenerte más tiempo del que quieres. Si algún día necesitamos financiación, será
              con publicidad coherente con lo que somos — y lo avisaremos.
            </p>
            <p class="m-0">Sin más.<br />Y recuerda: ¡Baila pogo!</p>
          </section>
        </div>

        <!-- English -->
        <div v-else class="flex flex-column gap-4 line-height-3 text-color-secondary">
          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">What is Rockalendar?</h2>
            <p class="m-0 mb-2">
              An app to find concerts and festivals of rock, metal, punk and everything else that comes with attitude.
              Anti-fascist cumbias welcome. Made by people from the scene, for people from the scene. No algorithms
              telling you what to like, no sponsorships hiding what doesn't pay.
            </p>
            <p class="m-0">Users add the events themselves. You can too. DIY and collaboration.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">Why there's moderation</h2>
            <p class="m-0 mb-2">
              Because not everything goes. Proposed events go through human review before they're published. Not because
              we don't trust you, but because we want to keep certain things out:
            </p>
            <ul class="m-0 pl-4 mb-2">
              <li>
                Festivals backed by promoters with unethical practices (labour exploitation, ties to predatory
                investment funds).
              </li>
              <li>Mainstream festivals: they already have plenty of platforms to advertise on.</li>
              <li>Bands that promote fascism, racism or any other form of hate.</li>
              <li>Misleading or outright false content.</li>
            </ul>
            <p class="m-0">If your proposal is rejected, you'll get an explanation. If it needs changes, same.</p>
          </section>

          <section>
            <h2 class="text-lg font-semibold mt-0 mb-2">Our deal with you</h2>
            <p class="m-0 mb-2">Culture is not a product. Neither are you.</p>
            <p class="m-0 mb-2">
              Rockalendar is and will remain free. No premium accounts, no paid featured events, no dark patterns
              designed to keep you longer than you want. If we ever need funding, it'll be through advertising coherent
              with what we are — and we'll be upfront about it.
            </p>
            <p class="m-0">That's it.<br />And remember: dance the pogo!</p>
          </section>
        </div>
      </template>
    </Card>
  </div>
</template>
