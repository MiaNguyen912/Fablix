/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./WebContent/**/*.{html,js}"],
  theme: {
    extend: {
      flexGrow: {
        '2': 2,
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/aspect-ratio'),
  ],
}

