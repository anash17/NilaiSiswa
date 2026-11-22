package com.guru.nilaisiswa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasScreen(vm: AppViewModel, onMaster: () -> Unit, onKelasClick: (Long) -> Unit) {
    val kelas by vm.kelas.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kelas") }, actions = {
                IconButton(onClick = onMaster) { Icon(Icons.Default.Settings, "Data master") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, "Tambah kelas")
            }
        }
    ) { pad ->
        if (kelas.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada kelas. Tekan tombol + untuk menambah.")
            }
        } else {
            LazyColumn(Modifier.padding(pad).fillMaxSize()) {
                items(kelas, key = { it.id }) { k ->
                    ListItem(
                        headlineContent = { Text(k.nama) },
                        modifier = Modifier.clickable { onKelasClick(k.id) },
                        trailingContent = {
                            TextButton(onClick = { vm.hapusKelas(k) }) { Text("Hapus") }
                        }
                    )
                }
            }
        }
    }

    if (showAdd) InputDialog("Nama Kelas",
        onDismiss = { showAdd = false },
        onConfirm = { vm.tambahKelas(it); showAdd = false })
}

@Composable
fun InputDialog(title: String, initial: String = "",
                onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it },
            label = { Text("Nama") }) },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
