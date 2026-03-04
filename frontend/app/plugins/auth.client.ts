export default defineNuxtPlugin(() => {
  useAuth().loadFromStorage();
});
