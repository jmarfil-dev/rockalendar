export function useUserDrawer() {
  const isOpen = useState<boolean>("userDrawer:isOpen", () => false);

  function open() {
    isOpen.value = true;
  }

  function close() {
    isOpen.value = false;
  }

  return {
    isOpen,
    open,
    close,
  };
}
