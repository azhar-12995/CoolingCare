package com.coolingcare.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.data.Graph
import com.coolingcare.app.model.Service
import com.coolingcare.app.platform.ImageDiskCache
import com.coolingcare.app.platform.decodeImage
import com.coolingcare.app.res.Res
import com.coolingcare.app.res.img_cat_split_ac
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decoded pictures kept in memory (most recent ~48). */
private object MemoryCache {
    private val map = LinkedHashMap<String, ImageBitmap>()
    fun get(key: String): ImageBitmap? = map[key]?.also { map.remove(key); map[key] = it }
    fun put(key: String, value: ImageBitmap) { map[key] = value; while (map.size > 48) map.remove(map.keys.first()) }
}

/** A Firebase Storage picture (order photo, uploaded service photo): memory → disk cache → download. */
@Composable
fun rememberStorageImage(path: String, minSide: Int = 400): ImageBitmap? {
    val key = "$path@$minSide"
    return produceState(MemoryCache.get(key), key) {
        if (value != null || path.isBlank()) return@produceState
        val bytes = withContext(Dispatchers.Default) { ImageDiskCache.get(path) }
            ?: runCatching { Graph.cloud.download(path) }.getOrNull()?.also { b -> withContext(Dispatchers.Default) { ImageDiskCache.put(path, b) } }
            ?: return@produceState
        value = withContext(Dispatchers.Default) { decodeImage(bytes, minSide) }?.also { MemoryCache.put(key, it) }
    }.value
}

/** A local JPEG (photos picked for an order, before upload). */
@Composable
fun rememberLocalImage(path: String, minSide: Int = 400): ImageBitmap? = produceState(MemoryCache.get("file:$path@$minSide"), path) {
    if (value != null) return@produceState
    value = withContext(Dispatchers.Default) { Graph.photos.read(path)?.let { decodeImage(it, minSide) } }?.also { MemoryCache.put("file:$path@$minSide", it) }
}.value

@Composable
fun StorageImage(path: String, modifier: Modifier, scale: ContentScale = ContentScale.Crop, minSide: Int = 400) {
    val bmp = rememberStorageImage(path, minSide)
    Box(modifier.background(CC.tile), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bmp, null, Modifier.fillMaxSize(), contentScale = scale)
        else Ic(CcIcons.Image, 20.dp, CC.placeholder)
    }
}

/**
 * Service picture: an admin-uploaded photo ([imagePath], Storage) or a bundled picture by key.
 * Product shots on a light background are shown whole; scenes are cropped.
 */
@Composable
fun ServicePicture(imageKey: String, imagePath: String, modifier: Modifier, minSide: Int = 400) {
    if (imagePath.isNotBlank()) { StorageImage(imagePath, modifier, ContentScale.Crop, minSide); return }
    val res = Catalog.images[imageKey] ?: Res.drawable.img_cat_split_ac
    Img(res, modifier.background(CC.tile), if (imageKey in Catalog.fitImages || imageKey !in Catalog.images) ContentScale.Fit else ContentScale.Crop)
}

@Composable
fun ServiceThumb(s: Service, modifier: Modifier) = ServicePicture(s.image, s.imagePath, modifier)

@Composable
fun ServiceHeader(s: Service, modifier: Modifier) = ServicePicture(s.header.ifBlank { s.image }, s.imagePath, modifier, minSide = 900)
