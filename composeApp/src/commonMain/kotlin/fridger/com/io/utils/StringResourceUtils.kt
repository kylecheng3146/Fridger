package fridger.com.io.utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * 用於格式化帶參數的字串資源的輔助函數
 * 解決 Compose Multiplatform 中 stringResource 參數化的限制問題
 */
@Composable
fun stringResourceFormat(resource: StringResource, vararg args: Any): String {
    val template = stringResource(resource)
    return template.format(*args)
}

/**
 * String 的擴展函數，用於跨平台的字串格式化
 */
fun String.format(vararg args: Any): String {
    var result = this
    args.forEachIndexed { index, arg ->
        result = result.replace("%s", arg.toString(), ignoreCase = false)
        result = result.replace("%d", arg.toString(), ignoreCase = false)
        result = result.replace("%${index + 1}\$s", arg.toString(), ignoreCase = false)
        result = result.replace("%${index + 1}\$d", arg.toString(), ignoreCase = false)
    }
    return result
}
