package com.guru.nilaisiswa

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guru.nilaisiswa.ui.AppViewModel
import com.guru.nilaisiswa.ui.NavGraph

class MainActivity : ComponentActivity() {

    private var onCsvResult: ((Uri) -> Unit)? = null

    private val createDoc = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { onCsvResult?.invoke(it) }
    }
    private val openDoc = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onCsvResult?.invoke(it) }
    }

    private fun launchCreate(name: String, block: (Uri) -> Unit) {
        onCsvResult = block; createDoc.launch(name)
    }
    private fun launchOpen(block: (Uri) -> Unit) {
        onCsvResult = block; openDoc.launch(arrayOf("text/*", "application/csv"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: AppViewModel =
                    viewModel(factory = AppViewModel.Factory(application as App))
                NavGraph(vm, contentResolver, ::launchCreate, ::launchOpen)
            }
        }
    }
}
