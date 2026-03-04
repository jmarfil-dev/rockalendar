export default defineNuxtRouteMiddleware((to) => {
  const { isAuthenticated } = useAuth();

  const protectedPaths = ["/me"];
  if (protectedPaths.some((p) => to.path.startsWith(p)) && !isAuthenticated.value) {
    return navigateTo("/login");
  }
});
