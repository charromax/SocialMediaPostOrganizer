package com.example.posteosdeig.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.util.*

enum class Branches {
    EIFFEL, PELTRE
}


@Entity(tableName = "article_table")
@Parcelize
data class Articulo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val collectionId: String = ""
) : Parcelable {
    override fun toString(): String {
        return "$title - $category"
    }
        }

@Entity(tableName = "collections_table")
@Parcelize
data class Coleccion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val branch: String,
    var isMarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    val formattedDate: String
    get() = DateFormat.getDateTimeInstance().format(createdAt)
}

@Parcelize
data class ColeccionWithArticulos(
    @Embedded val coleccion: Coleccion,
    @Relation(
        parentColumn = "id",
        entityColumn = "collectionId"
    )
    val article: List<Articulo>
) : Parcelable {
    override fun toString(): String {
        var lista = ""
        for (articulo in article) {
            lista += (articulo.toString() + "\n")
        }
        return "Coleccion: ${coleccion.name} \nARTICULOS: \n $lista"
    }
}