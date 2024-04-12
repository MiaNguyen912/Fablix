/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./WebContent/**/*.{html,js}"],
  theme: {
    extend: {},
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

