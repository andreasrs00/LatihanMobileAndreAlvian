package com.example.latihanmobileandrealvian

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.latihanmobileandrealvian.databinding.ActivityAddHomeworkBinding
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class AddHomework : AppCompatActivity() {
    private var isEdit = false
    private var homework: Homework? = null
    private var position: Int = 0
    private lateinit var homeworkHelper: HomeworkHelper

    private lateinit var binding: ActivityAddHomeworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeworkHelper = HomeworkHelper.getInstance(applicationContext)
        homeworkHelper.open()

        // Cek apakah ada data homework
        homework = intent.getParcelableExtra(EXTRA_HOMEWORK)
        if (homework != null) {
            position = intent.getIntExtra(EXTRA_POSITION,  0)
            isEdit = true
        } else {
            homework = Homework()
        }

        val actionBarTitle: String
        val btnTitle: String

        // Jika ada data pada homework berarti melakukan update
        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"

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
        binding.btnSubmit.setOnClickListener { addNewHomework() }

        binding.btnDelete.setOnClickListener { showAlertDialog(ALERT_DIALOG_DELETE) }
    }

    fun addNewHomework() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.error = "Title tidak boleh kosong"
            return
        }

        homework?.title = title
        homework?.description = description

        val intent = Intent()
        intent.putExtra(EXTRA_HOMEWORK, homework)
        intent.putExtra(EXTRA_POSITION, position)

        val values = ContentValues()
        values.put(DatabaseContract.HomeworkColumns.TITLE, title)
        values.put(DatabaseContract.HomeworkColumns.DESCRIPTION, description)

        if (isEdit) {
            val result = homeworkHelper.update(homework?.id.toString(), values)
            if (result > 0) {
                setResult(RESULT_UPDATE, intent)
                finish()
            } else {
                Toast.makeText(this, "Gagal mengupdate data", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            homework?.date = getCurrentDate()
            values.put(DatabaseContract.HomeworkColumns.DATE, getCurrentDate())
            val result = homeworkHelper.insert(values)

            if (result > 0) {
                homework?.id = result.toInt()
                setResult(RESULT_ADD, intent)
                finish()
            } else {
                Toast.makeText(this, "Gagal menambah data", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getCurrentDate() : String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan memperbarui data?"
        } else {
            dialogTitle = "Hapus Homework"
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
        }
    }
}