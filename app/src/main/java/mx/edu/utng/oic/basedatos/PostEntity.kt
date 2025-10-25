package mx.edu.utng.oic.basedatos

import androidx.room.Entity
import androidx.room.PrimaryKey

//Una entodad representa una tabla en la base de datos
@Entity(tableName = "posts")
data class PostEntity (
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    var content: String
)