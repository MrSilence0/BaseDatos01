package mx.edu.utng.oic.basedatos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy

// La función de DAO es definir las operaciones que se pueden realizar en la base de datos.
@Dao
interface PostDao {

    // Todas las operaciones de base de datos que bloquean el hilo principal deben ser 'suspend'.

    @Query("SELECT * FROM posts ORDER BY id Desc")
    suspend fun getAll(): List<PostEntity>

    // Se recomienda usar OnConflictStrategy.REPLACE para que 'insert' también sirva para actualizar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Delete
    suspend fun delete(post: PostEntity)

    @Update
    suspend fun update(post: PostEntity): Int

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: Int): PostEntity
}