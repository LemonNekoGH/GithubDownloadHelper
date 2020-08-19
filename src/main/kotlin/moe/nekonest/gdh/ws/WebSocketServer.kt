package moe.nekonest.gdh.ws

import org.springframework.web.socket.WebSocketSession

interface WebSocketServer {
    fun onOpen(session: WebSocketSession)
    fun onClose(session: WebSocketSession)
    fun onError(session: WebSocketSession, error: Throwable)
    fun onMessage(session: WebSocketSession, message: String)
}