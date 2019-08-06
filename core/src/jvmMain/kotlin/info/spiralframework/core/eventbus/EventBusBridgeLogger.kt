package info.spiralframework.core.eventbus

import java.util.logging.Level

class EventBusBridgeLogger(val logger: org.slf4j.Logger): org.greenrobot.eventbus.Logger {
    
    override fun log(level: Level?, msg: String?) {
        when (level) {
            Level.FINEST -> logger.trace(msg)
            Level.FINER -> logger.debug(msg)
            Level.FINE -> logger.debug(msg)
            Level.CONFIG -> logger.info(msg)
            Level.INFO -> logger.info(msg)
            Level.WARNING -> logger.warn(msg)
            Level.SEVERE -> logger.error(msg)
        }
    }

    override fun log(level: Level?, msg: String?, th: Throwable?) {
        when (level) {
            Level.FINEST -> logger.trace(msg, th)
            Level.FINER -> logger.debug(msg, th)
            Level.FINE -> logger.debug(msg, th)
            Level.CONFIG -> logger.info(msg, th)
            Level.INFO -> logger.info(msg, th)
            Level.WARNING -> logger.warn(msg, th)
            Level.SEVERE -> logger.error(msg, th)
        }
    }
}