package fridger.com.io

import android.annotation.SuppressLint
import android.content.Context

/**
 * A global context for the application.
 */
@SuppressLint("StaticFieldLeak")
lateinit var applicationContext: Context
    private set

/**
 * Sets the application context.
 *
 * @param context The context to be set.
 */
internal fun setApplicationContext(context: Context) {
    applicationContext = context.applicationContext
}
