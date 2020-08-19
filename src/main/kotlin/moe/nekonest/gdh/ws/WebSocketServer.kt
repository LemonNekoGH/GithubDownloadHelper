package moe.nekonest.gdh.ws

import javax.websocket.Session

interface WebSocketServer {
    fun onOpen(session: Session)
    fun onClose(session: Session)
    fun onError(session: Session, error: Throwable)
    fun onMessage(message: String, session: Session)
}