package fridger.com.io

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform