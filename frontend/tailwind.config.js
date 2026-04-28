/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        navio: {
          primary: '#2563eb',     // sky blue
          secondary: '#0ea5e9',
          dark: '#0f172a',
          accent: '#f59e0b',
        },
      },
      fontFamily: {
        sans: ['Pretendard', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
