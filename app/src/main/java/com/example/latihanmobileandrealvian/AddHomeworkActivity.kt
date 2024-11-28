package com.example.latihanmobileandrealvian

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.latihanmobileandrealvian.databinding.ActivityAddHomeworkBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AddHomeworkActivity : AppCompatActivity() {
    private var isEdit = false
    private var homework: Homework? = null
    private var position: Int = 0
    private lateinit var homeworkHelper: HomeworkHelper

    private lateinit var binding: ActivityAddHomeworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Membuka koneksi ke database
        homeworkHelper = HomeworkHelper.getInstance(applicationContext).apply {
            open()
        }

        // Memeriksa apakah sedang edit data
        homework = intent.getParcelableExtra(EXTRA_HOMEWORK)
        if (homework != null) {
            isEdit = true
            position = intent.getIntExtra(EXTRA_POSITION, 0)
        } else {
            homework = Homework()
        }

        setupActionBarAndButton()

        // Tombol untuk menyimpan data
        binding.btnSubmit.setOnClickListener { saveHomework() }

        // Tombol untuk menghapus data
        binding.btnDelete.setOnClickListener { showAlertDialog(ALERT_DIALOG_DELETE) }
    }

    private fun setupActionBarAndButton() {
        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"

            // Mengisi data jika sedang mengedit
            homework?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle
    }

    private fun saveHomework() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.error = "Title tidak boleh kosong"
            return
        }

        homework?.apply {
            this.title = title
            this.description = description
            if (!isEdit) {
                this.date = getCurrentDate()
            }
        }

        val values = ContentValues().apply {
            put(DatabaseContract.HomeworkColumns.TITLE, title)
            put(DatabaseContract.HomeworkColumns.DESCRIPTION, description)
            if (!isEdit) put(DatabaseContract.HomeworkColumns.DATE, getCurrentDate())
        }

        val intent = Intent().apply {
            putExtra(EXTRA_HOMEWORK, homework)
            putExtra(EXTRA_POSITION, position)
        }

        if (isEdit) {
            // Proses update data
            val result = homeworkHelper.update(homework?.id.toString(), values)
            if (result > 0) {
                setResult(RESULT_UPDATE, intent)
                finish()
            } else {
                showToast("Gagal mengupdate data")
            }
        } else {
            // Proses tambah data
            val result = homeworkHelper.insert(values)
            if (result > 0) {
                homework?.id = result.toInt()
                setResult(RESULT_ADD, intent)
                finish()
            } else {
                showToast("Gagal menambah data")
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val dialogTitle: String
        val dialogMessage: String

        if (type == ALERT_DIALOG_CLOSE) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan memperbarui data?"
        } else {
            dialogTitle = "Hapus Homework"
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (type == ALERT_DIALOG_CLOSE) {
                    finish()
                } else {
                    deleteHomework()
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun deleteHomework() {
        val result = homeworkHelper.deleteById(homework?.id.toString())
        if (result > 0) {
            val intent = Intent().apply {
                putExtra(EXTRA_POSITION, position)
            }
            setResult(RESULT_DELETE, intent)
            finish()
        } else {
            showToast("Gagal menghapus data")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_HOMEWORK = "extra_homework"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }
}