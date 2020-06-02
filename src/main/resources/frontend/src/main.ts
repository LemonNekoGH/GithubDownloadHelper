import Vue from 'vue'
import './plugins/axios'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import VueRouter, {RouteConfig} from "vue-router"
import Main from "@/components/Main.vue"
import About from "@/components/About.vue"

Vue.config.productionTip = false
Vue.use(VueRouter)

const routes: Array<RouteConfig> = [
  {
    path: "/",
    name: "Main",
    component: Main
  },
  {
    path: "/about",
    name: "About",
    component: About
  }
]

const router = new VueRouter({
  routes: routes
})

new Vue({
  vuetify,
  router,
  render: h => h(App)
}).$mount('#app')
