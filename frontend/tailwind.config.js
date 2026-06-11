/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      colors: {
        primary: {
          DEFAULT: '#1B3A6B',
          light: '#EFF6FF',
        },
      },
    },
  },
  plugins: [],
};
