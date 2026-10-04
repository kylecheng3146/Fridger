package fridger.com.io.data.connectivity

import java.net.NetworkInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

actual fun createConnectivityMonitor(): ConnectivityMonitor = object : ConnectivityMonitor {
    private val online = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = online

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            while (isActive) {
                // ponytail: interface availability; HTTP errors still retain pending work for retry.
                online.value = runCatching {
                    NetworkInterface.getNetworkInterfaces().toList().any { it.isUp && !it.isLoopback }
                }.getOrDefault(false)
                delay(5_000)
            }
        }
    }
}
