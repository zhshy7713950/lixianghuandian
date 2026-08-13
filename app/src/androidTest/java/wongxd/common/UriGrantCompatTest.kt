package wongxd.common

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UriGrantCompatTest {

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun readWriteGrant_setsFlagsAndClipData() {
        val uri = Uri.parse("content://test.authority/captured/image.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        UriGrantCompat.grantReadWrite(context, intent, uri)

        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertTrue(intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        assertEquals(1, intent.clipData?.itemCount)
        assertEquals(uri, intent.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun multipleReadGrant_carriesEveryUriWithoutWriteAccess() {
        val uris = listOf(
            Uri.parse("content://test.authority/shared/one.jpg"),
            Uri.parse("content://test.authority/shared/two.jpg")
        )
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

        UriGrantCompat.grantRead(context, intent, uris)

        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertFalse(intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        assertEquals(2, intent.clipData?.itemCount)
        assertEquals(uris[0], intent.clipData?.getItemAt(0)?.uri)
        assertEquals(uris[1], intent.clipData?.getItemAt(1)?.uri)
    }

    @Test
    fun fileProvider_isPrivateAndServesAppPrivateCache() {
        val authority = "${context.packageName}.fileprovider"
        val provider = requireNotNull(
            context.packageManager.resolveContentProvider(authority, PackageManager.GET_META_DATA)
        )
        assertFalse(provider.exported)
        assertTrue(provider.grantUriPermissions)

        val directory = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(directory, "provider-test.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val uri = FileProvider.getUriForFile(context, authority, file)

        assertEquals("content", uri.scheme)
        assertEquals(authority, uri.authority)
    }
}
