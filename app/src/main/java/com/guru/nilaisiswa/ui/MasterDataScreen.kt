package com.guru.nilaisiswa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen(vm: AppViewModel, onBack: () -> Unit) {
    val kategori by vm.kategori.collectAsState()
    val tp by vm.tp.collectAsState()
    var showKat by remember { mutableStateOf(false) }
    var showTp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategori & Tujuan Pembelajaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Row(Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kategori Penilaian", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showKat = true }) { Icon(Icons.Default.Add, "Tambah") }
            }
            if (kategori.isEmpty()) Text("Belum ada kategori.", Modifier.padding(horizontal = 8.dp))
            kategori.forEach { k ->
                ListItem(
                    headlineContent = { Text(k.nama) },
                    supportingContent = { Text("Bobot: ${k.bobot}") },
                    trailingContent = {
                        TextButton(onClick = { vm.hapusKategori(k) }) { Text("Hapus") }
                    }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Row(Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tujuan Pembelajaran", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showTp = true }) { Icon(Icons.Default.Add, "Tambah") }
            }
            LazyColumn {
                items(tp, key = { it.id }) { t ->
                    ListItem(
                        headlineContent = { Text(t.kode) },
                        supportingContent = { Text(t.deskripsi) },
                        trailingContent = {
                            TextButton(onClick = { vm.hapusTp(t) }) { Text("Hapus") }
                        }
                    )
                }
            }
        }
    }

    if (showKat) KategoriDialog(
        onDismiss = { showKat = false },
        onConfirm = { nama, bobot -> vm.tambahKategori(nama, bobot); showKat = false })
    if (showTp) DualInputDialog("Tambah TP", "Kode (mis. TP 1.1)", "Deskripsi",
        onDismiss = { showTp = false },
        onConfirm = { kode, desc -> vm.tambahTp(kode, desc); showTp = false })
}

@Composable
fun KategoriDialog(onDismiss: () -> Unit, onConfirm: (String, Float) -> Unit) {
    var nama by remember { mutableStateOf("") }
    var bobot by remember { mutableStateOf("1") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori Baru") },
        text = {
            Column {
                OutlinedTextField(nama, { nama = it }, label = { Text("Nama kategori") })
                OutlinedTextField(bobot, { bobot = it }, label = { Text("Bobot (default 1)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val b = bobot.toFloatOrNull() ?: 1f
                if (nama.isNotBlank() && b > 0f) onConfirm(nama.trim(), b)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
