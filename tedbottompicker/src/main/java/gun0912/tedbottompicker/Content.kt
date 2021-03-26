package gun0912.tedbottompicker

import android.net.Uri

data class Content(
    val uri: Uri,
    val type: Type,
    val duration: Long,
)
