import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

let prefix = "";
if (process.env.VUE_APP_MODE == "development"){
  prefix = "ws://localhost:4000/checkout"
}else if (process.env.VUE_APP_MODE == "production"){
  prefix = "ws://8.210.48.126:4000/checkout"
}

export enum State{
  READY,
  CHECKING_OUT,
  COMPRESSING,
  COMPLETED
}

let _ws = new WebSocket(prefix)
const messageListeners: Set<Function> = new Set<Function>()
const errorListeners: Set<Function> = new Set<Function>()

_ws.onmessage = function(this: WebSocket,e: MessageEvent){
  messageListeners.forEach(fun => {
    fun(this,e)
  })
}
_ws.onerror = function(this: WebSocket,ev: Event){
  errorListeners.forEach(fun => {
    fun(this,ev)
  })
}

_ws.onclose = function(this: WebSocket,ev: CloseEvent){
  alert("与服务器失去连接，请尝试刷新页面或与柠喵联系")
  window.location.reload()
}

let theState = State.READY

export default new Vuex.Store({
  state: {
    ws: _ws,
    theState: theState,
    checkingOutUrl: "",
    messageListeners: messageListeners,
    errorListeners: messageListeners,
    urlToDownload: "",
    downloadUrl: "",
    textFieldDisabled: false,
    btnDisabled: false,
    readyToDownload: false,
    showAppBarIcon: false
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})
