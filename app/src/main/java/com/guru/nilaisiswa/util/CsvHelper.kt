package com.guru.nilaisiswa.util

import com.guru.nilaisiswa.data.BarisNilai

object CsvHelper {

    private fun escape(f: String): String =
        if (f.contains(',') || f.contains('"') || f.contains('\n'))
            "\"${f.replace("\"", "\"\"")}\"" else f

    fun toCsv(rows: List<BarisNilai>, rekap: Map<Long, Float>,
              siswaById: Map<Long, com.guru.nilaisiswa.data.Siswa>): String {
        val sb = StringBuilder()
        sb.appendLine("NIS,Nama,Kategori,TP,Nilai")
        rows.forEach { r ->
            sb.appendLine(listOf(r.nis, r.namaSiswa, r.kategori, r.tp ?: "",
                r.nilai.toString()).joinToString(",") { escape(it) })
        }
        sb.appendLine()
        sb.appendLine("REKAP RATA-RATA")
        sb.appendLine("NIS,Nama,RataRata")
        rekap.forEach { (id, rata) ->
            val s = siswaById[id]
            sb.appendLine(listOf(s?.nis ?: "", s?.nama ?: "",
                String.format("%.2f", rata)).joinToString(",") { escape(it) })
        }
        return sb.toString()
    }

    /** Parse CSV dengan dukungan kutip RFC 4180. */
    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        var field = StringBuilder(); var row = mutableListOf<String>()
        var inQuotes = false; var i = 0
        val s = text.trimStart('﻿')
        while (i < s.length) {
            val c = s[i]
            when {
                inQuotes && c == '"' && i + 1 < s.length && s[i + 1] == '"' ->
                    { field.append('"'); i++ }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { row.add(field.toString()); field = StringBuilder() }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < s.length && s[i + 1] == '\n') i++
                    row.add(field.toString()); field = StringBuilder()
                    if (row.any { it.isNotBlank() }) rows.add(row)
                    row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows.filterIndexed { idx, r ->
            !(idx == 0 && r.firstOrNull()?.equals("NIS", true) == true)
        }
    }
}
