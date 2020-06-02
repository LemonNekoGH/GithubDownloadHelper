module.exports = {
  "transpileDependencies": [
    "vuetify"
  ],
  devServer: {
    open: true,
    host: "localhost",
    port: 8080,
    https: false,
    proxy: {
      "/api": {
        target: "http://localhost:4000",
        ws: true,
        changeOrigin: true,
        pathRewrite: {
          "^/api": ""
        }
      }
    }
  }
}