export function useNotificationsDrawer() {
  const isOpen = useState<boolean>("notificationsDrawer:isOpen", () => false);

  function open() {
    isOpen.value = true;
  }

  function close() {
    isOpen.value = false;
  }

  return { isOpen, open, close };
}
