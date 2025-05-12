package org.niaz.minimax.utils

import timber.log.Timber

/**
 * Simple log
 */
class MyDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        val fullClassName = element.className
        val simpleClassName = fullClassName.substringAfterLast('.')
        return "$simpleClassName.${element.methodName}"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "newapp", message, t)
    }
}

