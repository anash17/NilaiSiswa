package com.guru.nilaisiswa.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    fun semuaKelas() = db.kelasDao().semua()

    suspend fun tambahKelas(nama: String) {
        db.kelasDao().insert(Kelas(nama = nama.trim()))
    }

    suspend fun hapusKelas(k: Kelas) {
        db.kelasDao().delete(k)
    }

    fun siswaPerKelas(kelasId: Long) = db.siswaDao().perKelas(kelasId)

    suspend fun tambahSiswa(kelasId: Long, nis: String, nama: String) {
        val siswa = Siswa(
            kelasId = kelasId,
            nis = nis.trim(),
            nama = nama.trim()
        )
        db.siswaDao().insertAll(listOf(siswa))
    }

    fun semuaKategori() = db.kategoriDao().semua()

    suspend fun tambahKategori(nama: String, bobot: Float) {
        db.kategoriDao().insert(Kategori(nama = nama.trim(), bobot = bobot))
    }

    suspend fun hapusKategori(k: Kategori) {
        db.kategoriDao().delete(k)
    }

    fun semuaTp() = db.tpDao().semua()

    suspend fun tambahTp(kode: String, deskripsi: String) {
        db.tpDao().insert(TujuanPembelajaran(kode = kode.trim(), deskripsi = deskripsi.trim()))
    }

    suspend fun hapusTp(t: TujuanPembelajaran) {
        db.tpDao().delete(t)
    }

    fun barisPerKelas(kelasId: Long) = db.nilaiDao().barisPerKelas(kelasId)

    suspend fun simpanNilai(siswaId: Long, kategoriId: Long, tpId: Long?, nilai: Float) {
        db.withTransaction {
            db.nilaiDao().hapus(siswaId, kategoriId, tpId)
            val baru = Nilai(
                siswaId = siswaId,
                kategoriId = kategoriId,
                tpId = tpId,
                nilai = nilai
            )
            db.nilaiDao().upsertAll(listOf(baru))
        }
    }

    suspend fun hapusNilai(siswaId: Long, kategoriId: Long, tpId: Long?) {
        db.nilaiDao().hapus(siswaId, kategoriId, tpId)
    }

    /** Impor atomik dari baris CSV berformat: NIS,Nama,Kategori,TP,Nilai. */
    suspend fun impor(kelasId: Long, baris: List<List<String>>): Int = db.withTransaction {
        var count = 0
        val siswaCache = mutableMapOf<String, Long>()
        for (r in baris) {
            if (r.size < 5) continue
            val nis = r[0]
            val nama = r[1]
            val kat = r[2]
            val tp = r[3]
            val nilaiStr = r[4]
            val nilai = nilaiStr.toFloatOrNull() ?: continue
            if (nilai !in 0f..100f) continue

            val key = nis.trim()
            val siswaId = siswaCache.getOrPut(key) {
                val siswa = Siswa(kelasId = kelasId, nis = key, nama = nama.trim())
                db.siswaDao().insertAll(listOf(siswa)).first()
            }

            val kategoriId = db.kategoriDao().byNama(kat.trim())?.id
                ?: db.kategoriDao().insert(Kategori(nama = kat.trim()))
            val tpId = if (tp.isBlank()) null else
                db.tpDao().byKode(tp.trim())?.id
                    ?: db.tpDao().insert(TujuanPembelajaran(kode = tp.trim(), deskripsi = ""))

            db.nilaiDao().hapus(siswaId, kategoriId, tpId)
            val baru = Nilai(
                siswaId = siswaId,
                kategoriId = kategoriId,
                tpId = tpId,
                nilai = nilai
            )
            db.nilaiDao().upsertAll(listOf(baru))
            count++
        }
        count
    }

    /** Rekap: rata-rata berbobot per siswa dalam satu kelas. */
    suspend fun rekapKelas(kelasId: Long): List<RekapSiswa> {
        val semuaKategori = db.kategoriDao().semua().first()
        val bobot = semuaKategori.associate { it.id to it.bobot }
        val totalBobot = bobot.values.sum()

        return db.nilaiDao().nilaiPerKelas(kelasId)
            .groupBy { it.siswaId }
            .map { (siswaId, list) ->
                val perKategori = list.groupBy { it.kategoriId }
                    .mapValues { (_, v) -> v.map { it.nilai }.average().toFloat() }
                val rata = if (totalBobot > 0f)
                    perKategori.entries.sumOf { (id, avg) ->
                        (avg * (bobot[id] ?: 1f)).toDouble()
                    } / totalBobot
                else perKategori.values.average()
                RekapSiswa(siswaId, rata.toFloat())
            }
    }
}

data class RekapSiswa(val siswaId: Long, val rataRata: Float)
