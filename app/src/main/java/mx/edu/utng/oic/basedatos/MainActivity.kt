package mx.edu.utng.oic.basedatos

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.room.Room
import mx.edu.utng.oic.basedatos.ui.theme.BaseDatosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var postDao: PostDao
    private lateinit var db: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Asegúrate de que AppDataBase.getDatabase(this) es thread-safe
        db = AppDataBase.getDatabase(this)

        postDao = db.postDao()

        enableEdgeToEdge()
        setContent {
            BaseDatosTheme {
                // Pasamos el DAO al composable principal
                PantallaPrincipal(postDao)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    postDao: PostDao
) {
    // Necesitas un scope para lanzar corutinas
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var editingPost by remember { mutableStateOf<PostEntity?>(null) }

    // Función para refrescar la lista de posts
    fun refresh() {
        // La consulta de base de datos debe estar en una corutina
        scope.launch {
            posts = postDao.getAll()
        }
    }

    // Carga inicial de datos
    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi diario con Persistencia")
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            //CORRECCIÓN CLAVE: Envolver la operación de BD en una corutina
                            scope.launch {
                                if (editingPost == null) {
                                    // Guardar nuevo post
                                    postDao.insert(PostEntity(content = text))
                                } else {
                                    // Editar post existente
                                    // Se crea una copia del post con el ID original y el nuevo contenido
                                    // Asumiendo que tu @Insert tiene OnConflictStrategy.REPLACE
                                    val postToUpdate = editingPost!!.copy(content = text)
                                    postDao.insert(postToUpdate)
                                    editingPost = null
                                }
                                text = ""
                                refresh() // Refrescar la UI
                            }
                        },
                        // Deshabilitar si el campo de texto está vacío
                        enabled = text.isNotBlank()
                    ) {
                        if (editingPost == null) {
                            Text("Guardar")
                        } else {
                            Text("Editar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(post.content, modifier = Modifier.weight(1f))
                                Row {
                                    TextButton(onClick = {
                                        editingPost = post
                                        text = post.content
                                    }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = {
                                        //CORRECCIÓN CLAVE: Envolver la operación de BD en una corutina
                                        scope.launch {
                                            postDao.delete(post)
                                            refresh()
                                        }
                                    }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


/*
//Botón agregar publicación
floatingActionButton = {
    FloatingActionButton(onClick = {
        showDialog = true
    }) {
        Icon(Icons.Filled.Add, contentDescription = "Agregar publicación")
    }
}
) { innerPadding ->
Column(
    modifier = Modifier
        .padding(
            innerPadding
        )
        .fillMaxSize()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(posts) {
            post ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
            ) {
                Column(modifier.padding(16.dp)) {
                    Text(
                        post.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        if(posts.isEmpty()){
            item{
                Text("No tienes publicaciones.",
                    modifier = Modifier.padding(16.dp))
            }
        }
    }

}

}
if(showDialog){
NewPostDialog(
    onDismiss = {showDialog = false},
    onSave = {
        content:String ->
        postViewModel.addPost(content)
        showDialog = false
    }
)
}
}*/
/*
@Composable
fun NewPostDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
){
    var text by remember {mutableStateOf("")}
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nueva Publicación")
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {text=it},
                label = {"¿Qué estás pensando"}
            )
        },
        confirmButton = {
            Button(onClick= { onSave(text)},
                enabled = text.isNotBlank()){
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss){
                Text("Cancelar")
            }
        }
    )
}*/