package info.spiralframework.core.eventbus

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger

class LoggingSubscriber(val eventBus: EventBus, val logger: Logger) {
    @Subscribe
    fun log(event: Any) {
        logger.trace("core.eventbus.logging.event", eventBus, event)
    }

    init {
        eventBus.register(this)
    }
}