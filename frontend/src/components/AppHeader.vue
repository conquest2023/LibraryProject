<template>
  <header class="app-header">
    <div class="logo">
      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
      </svg>
      <h1>Book Radar</h1>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background-color: var(--card-background);
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}
.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  max-width: 1200px;
  margin: 0 auto;
}
.logo svg {
  color: var(--primary-color);
}
.logo h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-color);
}
</style>