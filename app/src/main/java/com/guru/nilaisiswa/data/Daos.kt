package com.guru.nilaisiswa.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface KelasDao {
    @Query("SELECT * FROM kelas ORDER BY nama") fun semua(): Flow<List<Kelas>>
    @Insert suspend fun insert(k: Kelas): Long
    @Update suspend fun update(k: Kelas)
    @Delete suspend fun delete(k: Kelas)
}

@Dao interface SiswaDao {
    @Query("SELECT * FROM siswa WHERE kelasId = :kelasId ORDER BY nama")
    fun perKelas(kelasId: Long): Flow<List<Siswa>>
    @Insert suspend fun insertAll(s: List<Siswa>): List<Long>
    @Delete suspend fun delete(s: Siswa)
}

@Dao interface KategoriDao {
    @Query("SELECT * FROM kategori ORDER BY nama") fun semua(): Flow<List<Kategori>>
    @Query("SELECT * FROM kategori WHERE nama = :nama LIMIT 1")
    suspend fun byNama(nama: String): Kategori?
    @Insert suspend fun insert(k: Kategori): Long
    @Delete suspend fun delete(k: Kategori)
}

@Dao interface TpDao {
    @Query("SELECT * FROM tp ORDER BY kode") fun semua(): Flow<List<TujuanPembelajaran>>
    @Query("SELECT * FROM tp WHERE kode = :kode LIMIT 1")
    suspend fun byKode(kode: String): TujuanPembelajaran?
    @Insert suspend fun insert(t: TujuanPembelajaran): Long
    @Delete suspend fun delete(t: TujuanPembelajaran)
}

@Dao interface NilaiDao {

    @Query("""
        SELECT n.siswaId, s.nis, s.nama, k.nama AS kategori, t.kode AS tp, n.nilai
        FROM nilai n
        JOIN siswa s ON s.id = n.siswaId
        JOIN kategori k ON k.id = n.kategoriId
        LEFT JOIN tp t ON t.id = n.tpId
        WHERE s.kelasId = :kelasId
        ORDER BY s.nama, k.nama, t.kode
    """)
    fun barisPerKelas(kelasId: Long): Flow<List<BarisNilai>>

    @Query("""
        SELECT n.* FROM nilai n
        JOIN siswa s ON s.id = n.siswaId
        WHERE s.kelasId = :kelasId
    """)
    suspend fun nilaiPerKelas(kelasId: Long): List<Nilai>

    @Query("SELECT * FROM nilai WHERE siswaId = :siswaId AND kategoriId = :kategoriId AND tpId IS :tpId LIMIT 1")
    suspend fun cari(siswaId: Long, kategoriId: Long, tpId: Long?): Nilai?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<Nilai>)

    @Query("DELETE FROM nilai WHERE siswaId = :siswaId AND kategoriId = :kategoriId AND tpId IS :tpId")
    suspend fun hapus(siswaId: Long, kategoriId: Long, tpId: Long?)
}
