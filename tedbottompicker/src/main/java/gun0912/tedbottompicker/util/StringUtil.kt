package gun0912.tedbottompicker.util

object StringUtil {
    @JvmStatic
    fun join(strs: List<String>, separator: String): String {
        return strs.joinToString(separator)
    }
}
