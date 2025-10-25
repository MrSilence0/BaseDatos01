package mx.edu.utng.oic.basedatos

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PostViewModel(application: Application): AndroidViewModel(application) {
    private val dao = Room.databaseBuilder(application,
        AppDataBase::class.java,"diario_db"
            ).build().postDao()

    private val _posts = mutableStateOf<List
    <PostEntity>>(emptyList())

    val posts: State<List<PostEntity>> = _posts

    init {
        //Cargar datos de las publicaciones
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _posts.value = dao.getAll()
        }
    }

    //Función que agrega una nueva publicación
    fun addPost(content: String) {
        viewModelScope.launch {
            //Insertar el contenido
            dao.insert(PostEntity(content = content))
            //Actualizar la lista
            loadPosts()
        }
    }

    //Función que elimina una publicación
    fun deletePost(post: PostEntity) {
        viewModelScope.launch {
            dao.delete(post)
            loadPosts()
        }
    }
}