package com.example.goldfinbudgeting

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.File

//opens a saved receipt photo in a simple dialog
object ReceiptViewer {

    fun show(context: Context, path: String?) {
        if (path.isNullOrBlank()) {
            Toast.makeText(context, "No receipt photo for this expense", Toast.LENGTH_SHORT).show()

            return
        }

        val pictureFile = File(path)

        if (!pictureFile.exists()) {
            Toast.makeText(context, "Receipt photo could not be found", Toast.LENGTH_SHORT).show()

            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_receipt_viewer, null)
        val receiptViewerImage = dialogView.findViewById<ImageView>(R.id.receiptViewerImage)

        receiptViewerImage.setImageURI(null)
        receiptViewerImage.setImageURI(Uri.fromFile(pictureFile))

        AlertDialog.Builder(context)
            .setTitle("Receipt")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    fun hasReceipt(path: String?): Boolean {
        return !path.isNullOrBlank() && File(path).exists()
    }
}
