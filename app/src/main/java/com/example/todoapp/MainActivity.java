package com.example.todoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private EditText editText;
    private Button addButton;
    private ArrayList<Task> taskList;
    private TaskAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editText  = findViewById(R.id.editText);
        addButton = findViewById(R.id.addButton);

        taskList = new ArrayList<>();
        adapter  = new TaskAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskTitle = editText.getText().toString().trim();
                if (!taskTitle.isEmpty()) {
                    taskList.add(0, new Task(taskTitle)); // Add new tasks to the top
                    updateUI();
                    editText.setText("");
                } else {
                    Toast.makeText(MainActivity.this,
                            R.string.toast_enter_task, Toast.LENGTH_SHORT).show();
                }
            }
        });

        setupDragAndDrop();
    }

    private void updateUI() {
        adapter.setTasks(taskList);
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                return adapter.handleMove(fromPos, toPos, taskList);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // Disable long press drag to use the handle instead
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onTaskStatusChanged() {
        updateUI();
    }

    @Override
    public void onTaskEdit(Task task) {
        showEditDialog(task);
    }

    @Override
    public void onTaskDelete(Task task) {
        showDeleteDialog(task);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void showEditDialog(final Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText input = dialogView.findViewById(R.id.editTaskInput);
        input.setText(task.getTitle());
        input.setSelection(input.getText().length());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String updatedTask = input.getText().toString().trim();
            if (!updatedTask.isEmpty()) {
                task.setTitle(updatedTask);
                updateUI();
                Toast.makeText(MainActivity.this, R.string.toast_task_updated, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteDialog(final Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    taskList.remove(task);
                    updateUI();
                    Toast.makeText(MainActivity.this, R.string.toast_task_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
