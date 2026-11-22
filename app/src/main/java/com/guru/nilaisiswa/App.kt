package com.guru.nilaisiswa

import android.app.Application
import com.guru.nilaisiswa.data.AppDatabase
import com.guru.nilaisiswa.data.Repository

class App : Application() {
    val repository: Repository by lazy { Repository(AppDatabase.get(this)) }
}
