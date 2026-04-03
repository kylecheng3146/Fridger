package fridger.com.io.data.connectivity

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}

expect fun createConnectivityMonitor(): ConnectivityMonitor

object ConnectivityMonitorProvider {
    val monitor: ConnectivityMonitor by lazy { createConnectivityMonitor() }
}
