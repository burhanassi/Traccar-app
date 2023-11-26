package com.logestechs.driver.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import com.example.tscdll.TSCActivity
import com.google.firebase.installations.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

class EnhancedTSCPrinterActivity : TSCActivity() {

    fun printImage(context: Context, imageFile: Bitmap) {
        openport(Helper.Companion.PrinterConst.PRINTER_BLUETOOTH_ADDRESS)
        clearbuffer()

        // Convert the bitmap to a byte array
//                    val byteArrayOutputStream = ByteArrayOutputStream()
//        imageFile.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//                    val byteArray = byteArrayOutputStream.toByteArray()

        // Send the byte array to the printer
        sendbitmap(0, 0, imageFile)

        // Perform any additional printing commands if needed
        // ...
    }


    fun printOnTscPrinter(context: Context?, file: File) {
        try {
            if (!IsConnected) openport(Helper.Companion.PrinterConst.PRINTER_BLUETOOTH_ADDRESS)
            clearbuffer()
            printPDF(file, 0, 0)
//            Utils.mainThreadHandler.post(Utils::hidePalshipDialog)
        } catch (e: Exception) {
//            Utils.mainThreadHandler.post {
//                Utils.hidePalshipDialog()
//                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
        }
    }

    fun printPDF(file: File, x_coordinates: Int, y_coordinates: Int) {
        try {
            pdf_gray_print_resize2fitWidth(x_coordinates, y_coordinates, file, 700)
        } catch (e: FileNotFoundException) {
            Log.e(LOG_TAG, e.message, e)
        }
    }

    @Throws(FileNotFoundException::class)
    override fun pdf_gray_print_resize2fitWidth(
        x_axis: Int,
        y_axis: Int,
        f1: File,
        resize_width: Int
    ): ArrayList<Bitmap> {
        val bitmaps = ArrayList<Bitmap>()
        return try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(f1, ParcelFileDescriptor.MODE_READ_WRITE))
            val pageCount = renderer.pageCount
            for (t in 0 until pageCount) {
                val page = renderer.openPage(t)
                val width = ((page.width * 72).toDouble() / 25.4).toInt()
                val height = ((page.height * 72).toDouble() / 25.4).toInt()
                val resize_height = resize_width * height / width
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val resize_bitmap =
                    Bitmap.createScaledBitmap(bitmap, resize_width, resize_height, false)
                val canvas = Canvas(resize_bitmap)
                canvas.drawColor(-1)
                canvas.drawBitmap(resize_bitmap, 0.0f, 0.0f, null as Paint?)
                page.render(
                    resize_bitmap,
                    null as Rect?,
                    null as Matrix?,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )
                bitmaps.add(resize_bitmap)
                page.close()
                var gray_bitmap: Bitmap?
                var binary_bitmap: Bitmap
                gray_bitmap = bitmap2Gray(resize_bitmap)
                binary_bitmap = this.gray2Binary(gray_bitmap)
                val picture_wdith = Integer.toString((resize_bitmap.width + 7) / 8)
                val picture_height = Integer.toString(resize_bitmap.height)
                val mode = Integer.toString(0)
                val command =
                    "BITMAP $x_axis,$y_axis,$picture_wdith,$picture_height,$mode,"
                val stream = ByteArray((resize_bitmap.width + 7) / 8 * resize_bitmap.height)
                val Width_bytes = (resize_bitmap.width + 7) / 8
                val Width = resize_bitmap.width
                val Height = resize_bitmap.height
                var y: Int
                y = 0
                while (y < Height * Width_bytes) {
                    stream[y] = -1
                    ++y
                }
                y = 0
                while (y < Height) {
                    for (x in 0 until Width) {
                        val pixelColor = binary_bitmap.getPixel(x, y)
                        val colorR = Color.red(pixelColor)
                        val colorG = Color.green(pixelColor)
                        val colorB = Color.blue(pixelColor)
                        val total = (colorR + colorG + colorB) / 3
                        if (total == 0) {
                            stream[y * ((Width + 7) / 8) + x / 8] =
                                (stream[y * ((Width + 7) / 8) + x / 8].toInt() xor (128 shr x % 8).toByte()
                                    .toInt()).toByte()
                        }
                    }
                    ++y
                }
                this.sendcommand("SIZE $resize_width dot, $resize_height dot\r\nCLS\r\n")
                this.sendcommand(command)
                sendlargebyte(stream)
                this.sendcommand("\r\nPRINT 1\r\n")
            }
            renderer.close()
            bitmaps
        } catch (var34: Exception) {
            var34.printStackTrace()
            bitmaps
        }
    }

    companion object {
        const val LOG_TAG = "EnhancedTSCActivity"
    }
}