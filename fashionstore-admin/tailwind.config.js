/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      colors: {
        // Shopify-inspired neutrals + restrained accents
        ink: {
          50: '#f6f6f7',
          100: '#ebebec',
          200: '#d6d6d8',
          300: '#aeaeb1',
          400: '#76767a',
          500: '#46464a',
          600: '#303033',
          700: '#202023',
          800: '#141416',
          900: '#0b0b0d',
        },
        accent: {
          DEFAULT: '#008060',   // Shopify green
          dark: '#005c45',
          light: '#e3f1ed',
        },
      },
      boxShadow: {
        card: '0 1px 2px rgba(15, 15, 15, 0.04), 0 1px 3px rgba(15, 15, 15, 0.06)',
        pop: '0 8px 24px rgba(15, 15, 15, 0.08)',
      },
      borderRadius: {
        xl: '12px',
      },
    },
  },
  plugins: [],
};
