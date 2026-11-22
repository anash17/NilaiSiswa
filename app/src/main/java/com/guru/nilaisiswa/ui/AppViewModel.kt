package com.guru.nilaisiswa.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guru.nilaisiswa.App
import com.guru.nilaisiswa.data.*
import com.guru.nilaisiswa.util.CsvHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AppViewModel(private val repo: Repository) : ViewModel() {

    val kelas = repo.semuaKelas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kategori = repo.semuaKategori()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tp = repo.semuaTp()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _kelasAktif = MutableStateFlow<Long?>(null)
    val kelasAktif: StateFlow<Long?> = _kelasAktif

    val siswa = _kelasAktif.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.siswaPerKelas(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val barisNilai = _kelasAktif.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.barisPerKelas(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rekap = MutableStateFlow<List<RekapSiswa>>(emptyList())
    val rekap: StateFlow<List<RekapSiswa>> = _rekap

    private val _pesan = MutableStateFlow<String?>(null)
    val pesan: StateFlow<String?> = _pesan

    fun pilihKelas(id: Long?) {
        _kelasAktif.value = id
        if (id != null) viewModelScope.launch { _rekap.value = repo.rekapKelas(id) }
    }

    fun tambahKelas(nama: String) = viewModelScope.launch { repo.tambahKelas(nama) }
    fun hapusKelas(k: Kelas) = viewModelScope.launch { repo.hapusKelas(k) }
    fun tambahSiswa(nis: String, nama: String) {
        val id = _kelasAktif.value ?: return
        viewModelScope.launch { repo.tambahSiswa(id, nis, nama) }
    }
    fun tambahKategori(nama: String, bobot: Float) =
        viewModelScope.launch { repo.tambahKategori(nama, bobot) }
    fun hapusKategori(k: Kategori) = viewModelScope.launch { repo.hapusKategori(k) }
    fun tambahTp(kode: String, deskripsi: String) =
        viewModelScope.launch { repo.tambahTp(kode, deskripsi) }
    fun hapusTp(t: TujuanPembelajaran) = viewModelScope.launch { repo.hapusTp(t) }

    fun simpanNilai(siswaId: Long, kategoriId: Long, tpId: Long?, nilai: Float) =
        viewModelScope.launch {
            repo.simpanNilai(siswaId, kategoriId, tpId, nilai)
            _kelasAktif.value?.let { _rekap.value = repo.rekapKelas(it) }
        }

    fun hapusNilai(siswaId: Long, kategoriId: Long, tpId: Long?) =
        viewModelScope.launch { repo.hapusNilai(siswaId, kategoriId, tpId) }

    fun imporCsv(uri: Uri, resolver: ContentResolver) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = resolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                } ?: error("Tidak bisa membuka file")
                val baris = CsvHelper.parse(text)
                val kelasId = _kelasAktif.value ?: error("Pilih kelas dulu")
                val jumlah = repo.impor(kelasId, baris)
                _rekap.value = repo.rekapKelas(kelasId)
                withContext(Dispatchers.Main) { _pesan.value = "$jumlah baris nilai diimpor" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _pesan.value = "Impor gagal: ${e.message}" }
            }
        }

    fun eksporCsv(uri: Uri, resolver: ContentResolver) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = barisNilai.value
                val rekapMap = _rekap.value.associate { it.siswaId to it.rataRata }
                val siswaMap = siswa.value.associateBy { it.id }
                val csv = CsvHelper.toCsv(rows, rekapMap, siswaMap)
                resolver.openOutputStream(uri, "wt")?.use { it.write(csv.toByteArray()) }
                withContext(Dispatchers.Main) { _pesan.value = "Berhasil diekspor" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _pesan.value = "Ekspor gagal: ${e.message}" }
            }
        }

    fun bersihkanPesan() { _pesan.value = null }

    class Factory(private val app: App) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            AppViewModel(app.repository) as T
    }
}
