package moe.nekonest.gdh.util

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.Session

object SessionMap {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val maps = ConcurrentHashMap<Session, ConcurrentHashMap<String, Any>>()

    fun register(session: Session) {
        if (session in this) {
            logger.error("session is already registered!")
            return
        }
        maps[session] = ConcurrentHashMap()
    }

    fun unregister(session: Session) {
        if (session !in this) {
            logger.error("session is not registered")
        }
        maps.remove(session)
    }

    operator fun contains(session: Session) = maps.containsKey(session)

    fun setValue(session: Session, key: String, value: Any) {
        if (session !in this) {
            logger.error("this session is not registered")
            return
        }
        maps[session]!![key] = value
    }

    fun getValue(session: Session, key: String): Any? {
        if (session !in this) {
            error("this session is not registered")
        }
        if (!maps[session]!!.containsKey(key)) {
            logger.error("key not found: $key")
            return null
        }
        return maps[session]!![key]!!
    }

    fun sessions() = maps.keys

    fun onlineNumber() = sessions().size.toString()
}