package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FileManagerRepository
import com.example.model.FileCategory
import com.example.model.FileItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testReadAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مدير الملفات", appName)
    }

    @Test
    fun testFileRepositoryOperations() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = FileManagerRepository(context)

            val root = repo.getRootDirectory()
            assertNotNull(root)

            // Test create folder
            val folderResult = repo.createFolder(root, "test_folder_${System.currentTimeMillis()}")
            assertTrue(folderResult.isSuccess)
            val folder = folderResult.getOrThrow()

            // Test create file inside folder
            val fileResult = repo.createFile(File(folder.path), "note.txt", "sample text content")
            assertTrue(fileResult.isSuccess)
            val file = fileResult.getOrThrow()

            // Test read text
            val text = repo.readText(File(file.path))
            assertEquals("sample text content", text)

            // Test format size
            val item = FileItem(path = file.path, name = file.name, size = 1024L)
            assertTrue(item.formattedSize.contains("1.0"))

            // Clean up
            repo.delete(File(file.path), moveToTrash = false)
            repo.delete(File(folder.path), moveToTrash = false)
        }
    }

    @Test
    fun testFileCategoryDetection() {
        assertEquals(FileCategory.IMAGES, FileCategory.fromExtension("jpg"))
        assertEquals(FileCategory.VIDEOS, FileCategory.fromExtension("mp4"))
        assertEquals(FileCategory.AUDIO, FileCategory.fromExtension("mp3"))
        assertEquals(FileCategory.DOCUMENTS, FileCategory.fromExtension("pdf"))
        assertEquals(FileCategory.ARCHIVES, FileCategory.fromExtension("zip"))
        assertEquals(FileCategory.APPS, FileCategory.fromExtension("apk"))

        val audioItem = FileItem(path = "/test.mp3", name = "test.mp3", extension = "mp3")
        assertTrue(audioItem.isAudioFile)

        val videoItem = FileItem(path = "/test.mp4", name = "test.mp4", extension = "mp4")
        assertTrue(videoItem.isVideoFile)

        val pdfItem = FileItem(path = "/test.pdf", name = "test.pdf", extension = "pdf")
        assertTrue(pdfItem.isPdfFile)

        val zipItem = FileItem(path = "/test.zip", name = "test.zip", extension = "zip")
        assertTrue(zipItem.isZipFile)

        val apkItem = FileItem(path = "/test.apk", name = "test.apk", extension = "apk")
        assertTrue(apkItem.isApkFile)
    }
}
