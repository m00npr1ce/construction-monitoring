<script setup lang="ts"></script>

<template>
  <div>
    <nav style="display:flex; gap:12px; margin-bottom:12px">
      <router-link to="/">Home</router-link>
      <router-link to="/auth">Login/Register</router-link>
      <router-link to="/projects">Projects</router-link>
      <router-link to="/defects">Defects</router-link>
    </nav>
    <router-view />
  </div>
</template>

<style scoped></style>
