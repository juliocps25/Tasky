package com.julio.tasky.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.julio.tasky.Activities.AddTaskActivity;
import com.julio.tasky.Adapter.TaskAdapter;
import com.julio.tasky.R;
import com.julio.tasky.Models.Task;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskClickListener, TaskAdapter.OnTaskCheckedChangeListener {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask; // Referencia al FAB

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db; // Instancia de Firestore

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Obtener instancia de Firestore
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewTasks = view.findViewById(R.id.rvTask); // Usar el ID correcto del layout fragment_home.xml
        fabAddTask = view.findViewById(R.id.fab); // Usar el ID correcto del layout fragment_home.xml

        // Configurar RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pasar 'this' como listeners ya que HomeFragment implementa las interfaces
        taskAdapter = new TaskAdapter(this, this);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Configurar OnClickListener para el FAB
        fabAddTask.setOnClickListener(v -> {
            // Acción al hacer clic en el FAB: Abrir pantalla para añadir tarea
            // Necesitarás crear un fragmento o actividad para añadir tareas
            // Aquí asumimos que tienes un fragmento llamado AddTaskFragment

            // --- Opción 1: Usando Navigation Component (Recomendado si lo usas) ---
            // Asegúrate de tener una acción definida en tu grafo de navegación
            // desde homeFragment a addTaskFragment (por ejemplo, action_homeFragment_to_addTaskFragment)
            // if (getView() != null) {
            //     Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_addTaskFragment);
            // }

            // --- Opción 2: Usando FragmentManager (Si no usas Navigation Component) ---
            // Reemplaza R.id.fragment_container por el ID de tu contenedor de fragmentos principal
            // if (getActivity() != null) {
            //     getActivity().getSupportFragmentManager().beginTransaction()
            //         .replace(R.id.fragment_container, new AddTaskFragment())
            //         .addToBackStack(null) // Opcional: permite volver a la lista de tareas
            //         .commit();
            // }

            if (getActivity() != null) {
                 Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                 startActivity(intent);
            }

            // Para este ejemplo, simplemente mostraremos un Toast si ninguna opción de navegación está implementada
            Toast.makeText(getContext(), "Botón Añadir Tarea presionado. Implementa la navegación.", Toast.LENGTH_SHORT).show();
        });


        // Obtener y mostrar tareas (se llama aquí y también en onResume para actualizar si se vuelve)
        fetchTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Volver a cargar las tareas cada vez que el fragmento se vuelve visible
        // Esto asegura que la lista se actualice si se añade, edita o elimina una tarea
        fetchTasks();
    }

    // Método para obtener las tareas de Firestore
    private void fetchTasks() {
        currentUser = mAuth.getCurrentUser(); // Obtener el usuario actual
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("tasks")
                    .whereEqualTo("userId", userId) // Filtrar por el UID del usuario actual
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por fecha de creación descendente
                    .addSnapshotListener((value, error) -> { // Usar SnapshotListener para actualizaciones en tiempo real
                        if (error != null) {
                            Log.w("HomeFragment", "Listen failed.", error);
                            Toast.makeText(getContext(), "Error al cargar tareas.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Task> tasks = new ArrayList<>();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                Task task = doc.toObject(Task.class);
                                task.setId(doc.getId()); // Establecer el ID del documento en el objeto Task
                                tasks.add(task);
                            }
                        }
                        taskAdapter.setTasks(tasks); // Actualizar el adaptador con la nueva lista
                    });
        } else {
            // Si el usuario no está autenticado, mostrar lista vacía y quizás redirigir
            taskAdapter.setTasks(new ArrayList<>());
            // Puedes añadir lógica aquí para redirigir al usuario al login si no está autenticado
            // Por ejemplo:
            // if (getActivity() != null) {
            //     Intent intent = new Intent(getActivity(), LoginActivity.class);
            //     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //     startActivity(intent);
            //     getActivity().finish();
            // }
        }
    }

    // <-- Implementar métodos de la interfaz TaskAdapter.OnTaskClickListener
    @Override
    public void onTaskClick(Task task) {
        // Aquí puedes implementar lo que sucede al hacer clic en una tarea
        // Por ejemplo, abrir una pantalla de detalles o edición
        Toast.makeText(getContext(), "Clic en tarea: " + task.getTitle(), Toast.LENGTH_SHORT).show();

        // --- Ejemplo de navegación a un fragmento de detalle usando Navigation Component ---
        // Asegúrate de tener una acción definida en tu grafo de navegación
        // desde homeFragment a taskDetailFragment (por ejemplo, action_homeFragment_to_taskDetailFragment)
        // y que taskDetailFragment pueda recibir el ID de la tarea en sus argumentos.
        // if (getView() != null && task.getId() != null) {
        //     Bundle bundle = new Bundle();
        //     bundle.putString("taskId", task.getId()); // Pasar el ID de la tarea al fragmento de detalle
        //     Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle);
        // }
    }

    // <-- Implementar métodos de la interfaz TaskAdapter.OnTaskCheckedChangeListener
    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        // Aquí implementas la lógica para actualizar el estado 'completado' en Firestore
        if (currentUser != null && task.getId() != null) {
            db.collection("tasks").document(task.getId())
                    .update("completed", isChecked) // Asegúrate de que el nombre del campo en Firestore sea "completed" si usas isCompleted() en tu clase Task
                    .addOnSuccessListener(aVoid -> {
                        Log.d("HomeFragment", "Estado de tarea actualizado correctamente.");
                        // Opcional: Mostrar un Toast
                        // Toast.makeText(getContext(), "Tarea " + (isChecked ? "completada" : "pendiente"), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("HomeFragment", "Error al actualizar estado de tarea.", e);
                        Toast.makeText(getContext(), "Error al actualizar tarea: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Si falla, podrías considerar revertir visualmente el CheckBox
                        // (esto requiere un poco más de lógica en el adaptador o manejar el estado localmente)
                    });
        }
    }
}