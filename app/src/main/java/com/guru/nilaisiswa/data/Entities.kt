package com.guru.nilaisiswa.data

import androidx.room.*

@Entity(tableName = "kelas")
data class Kelas(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String
)

@Entity(
    tableName = "siswa",
    foreignKeys = [ForeignKey(
        entity = Kelas::class, parentColumns = ["id"],
        childColumns = ["kelasId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("kelasId"), Index(value = ["kelasId", "nis"], unique = true)]
)
data class Siswa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kelasId: Long,
    val nis: String,
    val nama: String
)

@Entity(tableName = "kategori", indices = [Index(value = ["nama"], unique = true)])
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val bobot: Float = 1f
)

@Entity(tableName = "tp", indices = [Index(value = ["kode"], unique = true)])
data class TujuanPembelajaran(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kode: String,
    val deskripsi: String
)

@Entity(
    tableName = "nilai",
    foreignKeys = [
        ForeignKey(entity = Siswa::class, parentColumns = ["id"],
            childColumns = ["siswaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Kategori::class, parentColumns = ["id"],
            childColumns = ["kategoriId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = TujuanPembelajaran::class, parentColumns = ["id"],
            childColumns = ["tpId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("siswaId"), Index("kategoriId"), Index("tpId"),
               Index(value = ["siswaId", "kategoriId", "tpId"], unique = true)]
)
data class Nilai(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siswaId: Long,
    val kategoriId: Long,
    val tpId: Long?,
    val nilai: Float,
    val createdAt: Long = System.currentTimeMillis()
)

data class BarisNilai(
    @ColumnInfo(name = "siswaId") val siswaId: Long,
    @ColumnInfo(name = "nis") val nis: String,
    @ColumnInfo(name = "nama") val namaSiswa: String,
    @ColumnInfo(name = "kategori") val kategori: String,
    @ColumnInfo(name = "tp") val tp: String?,
    @ColumnInfo(name = "nilai") val nilai: Float
)
