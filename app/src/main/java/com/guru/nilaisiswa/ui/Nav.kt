package com.guru.nilaisiswa.ui

import android.content.ContentResolver
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(vm: AppViewModel, resolver: ContentResolver,
             launchCreate: (String, (android.net.Uri) -> Unit) -> Unit,
             launchOpen: ((android.net.Uri) -> Unit) -> Unit) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "kelas") {
        composable("kelas") {
            KelasScreen(vm, onMaster = { nav.navigate("master") }) {
                vm.pilihKelas(it); nav.navigate("detail")
            }
        }
        composable("detail") {
            KelasDetailScreen(vm, resolver, launchCreate, launchOpen) { nav.popBackStack() }
        }
        composable("master") {
            MasterDataScreen(vm) { nav.popBackStack() }
        }
    }
}
