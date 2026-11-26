package com.example.animedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.animedex.ui.theme.AnimeDexTheme
import androidx.compose.runtime.produceState
import com.example.animedex.ui.theme.AnimeDatabase
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnimeDexTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val mensaje = produceState("Cargando…") {
                        val dao = AnimeDatabase.get(applicationContext).animeDao()
                        val ficha = dao.detail(28977).first()
                        value = "${ficha?.anime?.title}: ${ficha?.genres?.size} géneros"
                    }

                    Text(mensaje.value)
                }
            }
        }
    }
}