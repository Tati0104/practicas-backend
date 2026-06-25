/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      colors: {
        primary: {
          DEFAULT: '#19426B',
          light: '#E8EEF4',
          dark: '#123052',
          accent: '#2E6DA8',
          glow: '#3B82F6',
        },
        dark: {
          base: '#0B0F14',
          card: '#151C26',
          elevated: '#1C2634',
          border: '#2A3544',
          sidebar: '#0A1628',
        },
      },
      boxShadow: {
        card: '0 4px 24px rgba(0, 0, 0, 0.25)',
        'card-light': '0 1px 3px rgba(0, 0, 0, 0.08)',
      },
    },
  },
  plugins: [],
};
