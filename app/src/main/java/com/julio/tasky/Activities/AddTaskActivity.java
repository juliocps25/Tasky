package com.julio.tasky.Activities;

import android.os.Bundle;
import android.text.TextUtils; // Importar TextUtils
import android.util.Log; // Importar Log para depuración
import android.widget.Toast;

import androidx.activity.EdgeToEdge; // Cambiado a EdgeToEdge
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets; // Importar Insets
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Importar FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore;
import com.julio.tasky.Models.Task;
import com.julio.tasky.R;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTaskTitle;
    private TextInputEditText editTextTaskDescription;
    private MaterialButton buttonSaveTask;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final String TAG = "AddTaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);

        buttonSaveTask.setOnClickListener(view -> {
            saveTask();
        });
    }

    private void saveTask() {
        String title = editTextTaskTitle.getText().toString().trim();
        String description = editTextTaskDescription.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (TextUtils.isEmpty(title)) {
            editTextTaskTitle.setError("Título es requerido");
            editTextTaskTitle.requestFocus();
            return;
        }
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. No se puede guardar la tarea.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Usuario no autenticado. No se puede guardar la tarea.");
            return;
        }

        Task newTask = new Task(
                currentUser.getUid(),
                title,
                description,
                false
        );

        db.collection("tasks") // "tasks" es el nombre de tu colección en Firestore
                .add(newTask) // add() agrega un nuevo documento con un ID autogenerado
                .addOnSuccessListener(documentReference -> {
                    // Éxito al guardar la tarea
                    Toast.makeText(AddTaskActivity.this, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()); // Log del ID del documento
                    // Puedes finalizar la actividad para volver a la pantalla anterior
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error al guardar la tarea
                    Toast.makeText(AddTaskActivity.this, "Error al guardar tarea: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e); // Logear el error para depuración
                });
    }
}