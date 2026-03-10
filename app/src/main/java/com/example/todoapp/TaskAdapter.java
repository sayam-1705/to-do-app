package com.example.todoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private Context context;
    private List<Task> activeTasks = new ArrayList<>();
    private List<Task> completedTasks = new ArrayList<>();
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskStatusChanged();
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public TaskAdapter(Context context, OnTaskActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<Task> allTasks) {
        activeTasks.clear();
        completedTasks.clear();
        for (Task task : allTasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                activeTasks.add(task);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position <= activeTasks.size()) return TYPE_ITEM;
        if (position == activeTasks.size() + 1) return TYPE_HEADER;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            if (position == 0) {
                headerHolder.title.setText(R.string.header_todo);
                headerHolder.title.setVisibility(activeTasks.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                headerHolder.title.setText(R.string.header_completed);
                headerHolder.title.setVisibility(completedTasks.isEmpty() ? View.GONE : View.VISIBLE);
            }
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            final Task task;
            if (position <= activeTasks.size()) {
                task = activeTasks.get(position - 1);
            } else {
                task = completedTasks.get(position - activeTasks.size() - 2);
            }

            taskHolder.titleView.setText(task.getTitle());
            taskHolder.checkBox.setOnCheckedChangeListener(null);
            taskHolder.checkBox.setChecked(task.isCompleted());
            updateStrikeThrough(taskHolder.titleView, task.isCompleted());

            // Checkbox listener
            taskHolder.checkBox.setOnClickListener(v -> {
                task.setCompleted(taskHolder.checkBox.isChecked());
                if (listener != null) listener.onTaskStatusChanged();
            });

            // Edit icon listener
            taskHolder.editIcon.setOnClickListener(v -> {
                if (listener != null) listener.onTaskEdit(task);
            });

            // Delete icon listener
            taskHolder.deleteIcon.setOnClickListener(v -> {
                if (listener != null) listener.onTaskDelete(task);
            });

            // Reorder handle listener
            taskHolder.reorderHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (listener != null) listener.onStartDrag(taskHolder);
                }
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return false;
            });

            // Entire card tap to edit
            taskHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskEdit(task);
            });

            // Entire card long press to delete
            taskHolder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onTaskDelete(task);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        count += 1 + activeTasks.size(); // Header + Active tasks
        count += 1 + completedTasks.size(); // Header + Completed tasks
        return count;
    }

    public boolean handleMove(int fromPos, int toPos, List<Task> masterList) {
        if (getItemViewType(fromPos) != TYPE_ITEM || getItemViewType(toPos) != TYPE_ITEM) return false;
        
        boolean fromActive = fromPos <= activeTasks.size();
        boolean toActive = toPos <= activeTasks.size();
        
        if (fromActive != toActive) return false;

        Task fromTask = getTaskAt(fromPos);
        Task toTask = getTaskAt(toPos);
        
        int mFrom = masterList.indexOf(fromTask);
        int mTo = masterList.indexOf(toTask);
        
        if (mFrom != -1 && mTo != -1) {
            Collections.swap(masterList, mFrom, mTo);
            
            if (fromActive) {
                Collections.swap(activeTasks, fromPos - 1, toPos - 1);
            } else {
                Collections.swap(completedTasks, fromPos - activeTasks.size() - 2, toPos - activeTasks.size() - 2);
            }
            
            notifyItemMoved(fromPos, toPos);
            return true;
        }
        return false;
    }

    private Task getTaskAt(int position) {
        if (position <= activeTasks.size()) {
            return activeTasks.get(position - 1);
        } else {
            return completedTasks.get(position - activeTasks.size() - 2);
        }
    }

    private void updateStrikeThrough(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView titleView;
        ImageView editIcon, deleteIcon, reorderHandle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
            titleView = itemView.findViewById(R.id.taskTitle);
            editIcon = itemView.findViewById(R.id.editIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            reorderHandle = itemView.findViewById(R.id.reorderHandle);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.headerTitle);
        }
    }
}
