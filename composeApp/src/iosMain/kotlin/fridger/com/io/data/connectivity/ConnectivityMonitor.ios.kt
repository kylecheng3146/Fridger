package fridger.com.io.data.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Network.NWPathMonitor
import platform.Network.NWPathStatusSatisfied
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_queue_priority_t

class IosConnectivityMonitor : ConnectivityMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline

    private val monitor = NWPathMonitor()

    init {
        _isOnline.value = monitor.currentPath?.status == NWPathStatusSatisfied
        monitor.setUpdateHandler { path ->
            _isOnline.value = path.status == NWPathStatusSatisfied
        }
        monitor.startQueue(dispatch_get_global_queue(dispatch_queue_priority_t(0), 0))
    }
}

actual fun createConnectivityMonitor(): ConnectivityMonitor = IosConnectivityMonitor()
