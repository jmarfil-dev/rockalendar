import { ROUTE_PATH } from "~/constants/routes";

export interface EventComment {
  id: string;
  authorEmail: string;
  authorName: string | null;
  body: string;
  createdAt: string;
}

export interface PostCommentPayload {
  authorEmail?: string;
  authorName?: string;
  body: string;
}

export function useEventComments() {
  const { t } = useI18n();
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function postComment(eventId: string, payload: PostCommentPayload): Promise<boolean> {
    loading.value = true;
    error.value = null;
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiEventComment(eventId), {
      method: "POST",
      body: payload,
    });
    loading.value = false;
    if (!res.ok) error.value = extractApiError(res, t);
    return res.ok;
  }

  return { loading, error, postComment };
}

export function useModerationComments() {
  const { t } = useI18n();
  const comments = ref<EventComment[]>([]);
  const loading = ref(false);
  const deleteLoading = ref<string | null>(null);
  const error = ref<string | null>(null);

  async function fetchComments(eventId: string) {
    loading.value = true;
    const res = await fetchAuthResult<EventComment[]>(ROUTE_PATH.apiModerationEventComments(eventId));
    loading.value = false;
    if (res.ok) {
      comments.value = res.data;
    } else {
      error.value = extractApiError(res, t);
    }
  }

  async function deleteComment(eventId: string, commentId: string): Promise<boolean> {
    deleteLoading.value = commentId;
    const res = await fetchAuthResult<undefined>(ROUTE_PATH.apiModerationEventCommentDelete(eventId, commentId), {
      method: "DELETE",
    });
    deleteLoading.value = null;
    if (res.ok) {
      comments.value = comments.value.filter((c) => c.id !== commentId);
    } else {
      error.value = extractApiError(res, t);
    }
    return res.ok;
  }

  return { comments, loading, deleteLoading, error, fetchComments, deleteComment };
}
