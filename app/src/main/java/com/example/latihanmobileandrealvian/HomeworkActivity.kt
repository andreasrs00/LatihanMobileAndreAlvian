package com.example.latihanmobileandrealvian

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.latihanmobileandrealvian.databinding.ActivityHomeworkBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeworkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeworkBinding
    private lateinit var adapter: HomeworkAdapter

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let { data ->
                when (result.resultCode) {
                    AddHomeworkActivity.RESULT_ADD -> {
                        val homework = data.getParcelableExtra<Homework>(AddHomeworkActivity.EXTRA_HOMEWORK)
                        homework?.let {
                            adapter.addItem(it)
                            binding.rvHomework.smoothScrollToPosition(adapter.itemCount - 1)
                            showSnackbarMessage("Data berhasil ditambahkan")
                        }
                    }
                    AddHomeworkActivity.RESULT_UPDATE -> {
                        val homework = data.getParcelableExtra<Homework>(AddHomeworkActivity.EXTRA_HOMEWORK)
                        val position = data.getIntExtra(AddHomeworkActivity.EXTRA_POSITION, -1)
                        if (homework != null && position >= 0) {
                            adapter.updateItem(position, homework)
                            binding.rvHomework.smoothScrollToPosition(position)
                            showSnackbarMessage("Data berhasil diubah")
                        }
                    }
                    AddHomeworkActivity.RESULT_DELETE -> {
                        val position = data.getIntExtra(AddHomeworkActivity.EXTRA_POSITION, -1)
                        if (position >= 0) {
                            adapter.removeItem(position)
                            showSnackbarMessage("Data berhasil dihapus")
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Homework"
        binding.rvHomework.layoutManager = LinearLayoutManager(this)
        binding.rvHomework.setHasFixedSize(true)

        adapter = HomeworkAdapter(object : HomeworkAdapter.OnItemClickCallback {
            override fun onItemClicked(selectedHomework: Homework?, position: Int?) {
                val intent = Intent(this@HomeworkActivity, AddHomeworkActivity::class.java).apply {
                    putExtra(AddHomeworkActivity.EXTRA_HOMEWORK, selectedHomework)
                    putExtra(AddHomeworkActivity.EXTRA_POSITION, position)
                }
                resultLauncher.launch(intent)
            }
        })
        binding.rvHomework.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddHomeworkActivity::class.java)
            resultLauncher.launch(intent)
        }

        if (savedInstanceState == null) {
            loadHomeworkAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<Homework>(EXTRA_STATE)
            list?.let { adapter.listHomework = it }
        }
    }

    private fun loadHomeworkAsync() {
        lifecycleScope.launch {
            val homeworkHelper = HomeworkHelper.getInstance(applicationContext)
            homeworkHelper.open()
            val deferredHomework = async(Dispatchers.IO) {
                val cursor = homeworkHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val homework = deferredHomework.await()
            adapter.listHomework = homework.ifEmpty {
                showSnackbarMessage("Data tidak ada")
                ArrayList()
            }
            homeworkHelper.close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, ArrayList(adapter.listHomework))
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.rvHomework, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }
}