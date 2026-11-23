package com.guru.nilaisiswa.ui

import android.content.ContentResolver
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasDetailScreen(vm: AppViewModel, resolver: ContentResolver,
                      launchCreate: (String, (android.net.Uri) -> Unit) -> Unit,
                      launchOpen: ((android.net.Uri) -> Unit) -> Unit,
                      onBack: () -> Unit) {
    val siswa by vm.siswa.collectAsState()
    val kategori by vm.kategori.collectAsState()
    val tp by vm.tp.collectAsState()
    val baris by vm.barisNilai.collectAsState()
    val rekap by vm.rekap.collectAsState()
    val pesan by vm.pesan.collectAsState()

    var katId by remember { mutableStateOf<Long?>(null) }
    var tpId by remember { mutableStateOf<Long?>(null) }
    var showAddSiswa by remember { mutableStateOf(false) }
    var showRekap by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(pesan) {
        pesan?.let { snackbar.showSnackbar(it); vm.bersihkanPesan() }
    }

    val katTerpilih = kategori.firstOrNull { it.id == katId }
    val tpTerpilih = tp.firstOrNull { it.id == tpId }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Input Nilai") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showRekap = !showRekap }) {
                        Icon(Icons.Default.Assessment, "Rekap")
                    }
                    IconButton(onClick = {
                        launchCreate("nilai_${System.currentTimeMillis()}.csv") { uri ->
                            vm.eksporCsv(uri, resolver)
                        }
                    }) { Icon(Icons.Default.Upload, "Ekspor CSV") }
                    IconButton(onClick = {
                        launchOpen { uri -> vm.imporCsv(uri, resolver) }
                    }) { Icon(Icons.Default.Download, "Impor CSV") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSiswa = true }) {
                Icon(Icons.Default.Add, "Tambah siswa")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Box(Modifier.weight(1f)) {
                    DropdownSelector(
                        label = "Kategori",
                        value = katTerpilih?.nama ?: "",
                        options = kategori.map { it.nama },
                        onPick = { idx -> katId = kategori[idx].id }
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    val opsi = listOf("(tanpa TP)") + tp.map { it.kode }
                    DropdownSelector(
                        label = "TP",
                        value = tpTerpilih?.kode ?: "",
                        options = opsi,
                        onPick = { idx -> tpId = if (idx == 0) null else tp[idx - 1].id }
                    )
                }
            }

            if (kategori.isEmpty()) {
                Text(
                    "Kategori masih kosong. Buka ikon gear di layar Kelas untuk menambah kategori dan TP.",
                    Modifier.padding(16.dp)
                )
            }

            LazyColumn(Modifier.weight(1f)) {
                items(siswa, key = { it.id }) { s ->
                    val existing = baris.firstOrNull {
                        it.siswaId == s.id && it.kategori == (katTerpilih?.nama ?: "")
                    }?.nilai?.let {
                        if (it == it.toLong().toFloat()) it.toLong().toString() else it.toString()
                    } ?: ""
                    var text by remember(s.id, katId, tpId) { mutableStateOf(existing) }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.nama, style = MaterialTheme.typography.bodyLarge)
                            Text("NIS ${s.nis}", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.width(90.dp),
                            singleLine = true,
                            label = { Text("Nilai") }
                        )
                        TextButton(onClick = {
                            val n = text.toFloatOrNull()
                            val k = katId
                            if (n != null && n in 0f..100f && k != null) {
                                vm.simpanNilai(s.id, k, tpId, n)
                            }
                        }) { Text("Simpan") }
                    }
                }
            }

            if (showRekap) {
                Surface(tonalElevation = 2.dp) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("Rekap Rata-rata Berbobot",
                            style = MaterialTheme.typography.titleMedium)
                        if (rekap.isEmpty()) Text("Belum ada nilai.")
                        rekap.forEach { r ->
                            val nama = siswa.firstOrNull { it.id == r.siswaId }?.nama ?: "?"
                            Text("$nama: %.2f".format(r.rataRata))
                        }
                    }
                }
            }
        }
    }

    if (showAddSiswa) DualInputDialog("Tambah Siswa", "NIS", "Nama",
        onDismiss = { showAddSiswa = false },
        onConfirm = { nis, nama -> vm.tambahSiswa(nis, nama); showAddSiswa = false })
}

/** Dropdown sederhana dan stabil: teks baca-saja + menu. */
@Composable
fun DropdownSelector(label: String, value: String,
                     options: List<String>, onPick: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
        // lapisan transparan agar seluruh area bisa diklik
        Box(
            Modifier.matchParentSize().clickable { expanded = true }
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("(kosong)") }, onClick = { expanded = false })
            }
            options.forEachIndexed { i, o ->
                DropdownMenuItem(text = { Text(o) }, onClick = { onPick(i); expanded = false })
            }
        }
    }
}

@Composable
fun DualInputDialog(title: String, l1: String, l2: String,
                    onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var f1 by remember { mutableStateOf("") }
    var f2 by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(f1, { f1 = it }, label = { Text(l1) })
                OutlinedTextField(f2, { f2 = it }, label = { Text(l2) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (f1.isNotBlank() && f2.isNotBlank()) onConfirm(f1.trim(), f2.trim())
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
