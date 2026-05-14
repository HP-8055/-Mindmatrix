package com.example.nammaplatform

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

data class Train(
    val id: String = "",
    val name: String = "",
    val platform: String = "",
    val time: String = "",
    val coaches: List<String> = emptyList()
)

fun loadTrainsFromAssets(context: Context): List<Train> {
    return try {
        val jsonString = context.assets.open("trains.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Train>>() {}.type
        Gson().fromJson(jsonString, listType)
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun fetchTrainsFromFirestore(): List<Train> {
    return try {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("trains").get().await()
        snapshot.toObjects(Train::class.java)
    } catch (e: Exception) {
        // Log error or handle failure
        emptyList()
    }
}