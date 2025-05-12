package com.julio.tasky.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.julio.tasky.Models.Task;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.julio.tasky.R;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // Utiliza tu clase Task aquí
    private List<Task> tasks = new ArrayList<>();
    private OnTaskClickListener onTaskClickListener; // Interfaz para manejar clics
    private OnTaskCheckedChangeListener onTaskCheckedChangeListener; // Interfaz para CheckBox

    // Interfaz para manejar clics en los elementos de la lista
    public interface OnTaskClickListener {
        void onTaskClick(Task task); // Utiliza tu clase Task
    }

    // Interfaz para manejar cambios en el CheckBox
    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked); // Utiliza tu clase Task
    }

    // Constructor
    public TaskAdapter(OnTaskClickListener onTaskClickListener, OnTaskCheckedChangeListener onTaskCheckedChangeListener) {
        this.onTaskClickListener = onTaskClickListener;
        this.onTaskCheckedChangeListener = onTaskCheckedChangeListener;
    }

    // Método para actualizar la lista de tareas en el adaptador
    public void setTasks(List<Task> tasks) { // Utiliza tu clase Task
        this.tasks = tasks;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout para cada elemento de la lista. Asegúrate de que R.layout.item_task exista.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Obtiene la tarea actual en esta posición
        Task currentTask = tasks.get(position); // Utiliza tu clase Task

        // Configura las vistas con los datos de la tarea
        holder.textViewTitle.setText(currentTask.getTitle());

        // Importante: Limpiar listener antes de establecer el estado para evitar disparadores
        holder.checkBoxCompleted.setOnCheckedChangeListener(null);
        holder.checkBoxCompleted.setChecked(currentTask.isCompleted());

        // Configurar listener de clic para el elemento completo (opcional)
        holder.itemView.setOnClickListener(v -> {
            if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(currentTask);
            }
        });

        // Configurar listener para el CheckBox
        holder.checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onTaskCheckedChangeListener != null) {
                // Aquí puedes actualizar el estado 'completed' en el objeto Task si lo necesitas localmente
                // currentTask.setCompleted(isChecked);
                onTaskCheckedChangeListener.onTaskCheckedChanged(currentTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // ViewHolder interno para mantener las vistas de cada elemento de la lista
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        // Referencias a las vistas dentro del layout item_task.xml
        public TextView textViewTitle;
        public CheckBox checkBoxCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encuentra las vistas por sus IDs en item_task.xml
            textViewTitle = itemView.findViewById(R.id.textViewTaskTitle);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxTaskCompleted);
        }
    }
}