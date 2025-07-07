package fridger.com.io

import android.app.Application

/**
 * The application class.
 */
class FridgerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the application context
        setApplicationContext(this)
    }
}
