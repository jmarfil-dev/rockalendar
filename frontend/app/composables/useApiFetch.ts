export async function useApiFetch<ResT = unknown, ReqT = ResT>(
  request: Parameters<typeof useFetch<ResT, ReqT>>[0],
  opts: Parameters<typeof useFetch<ResT, ReqT>>[1] = {},
) {
  const result = await useFetch<ResT, ReqT>(request, opts);

  if (result.error.value) {
    const anyErr = result.error.value as any;

    throw createError({
      status: anyErr?.status ?? anyErr?.statusCode ?? 500, // Nuxt 4 usa status
      statusText: anyErr?.statusText ?? anyErr?.statusMessage ?? "Request failed",
      data: anyErr?.data, // ProblemDetail del backend
    });
  }

  return result;
}
