package com.guru.nilaisiswa.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [Kelas::class, Siswa::class, Kategori::class,
                TujuanPembelajaran::class, Nilai::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kelasDao(): KelasDao
    abstract fun siswaDao(): SiswaDao
    abstract fun kategoriDao(): KategoriDao
    abstract fun tpDao(): TpDao
    abstract fun nilaiDao(): NilaiDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "nilai_siswa.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
