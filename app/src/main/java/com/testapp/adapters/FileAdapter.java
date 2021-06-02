package com.testapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.testapp.R;
import com.testapp.models.File;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    List<File> fileList = new ArrayList<>();

    public FileAdapter() {
//        this.fileList = new ArrayList<>();
    }

    public void updateData(List<File> files) {
        this.fileList = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item,
                        parent,
                        false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = fileList.get(position);
        holder.name.setText("Name: "+file.getName());
        holder.size.setText("Size: "+String.valueOf(file.getSize()));
        holder.path.setText("Path: "+file.getPath());
        holder.type.setText(file.getType());

    }

    @Override
    public int getItemCount() {
        if (fileList != null && fileList.size() >= 1)
            return fileList.size();
        return 0;
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView size;
        TextView type;
        TextView path;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txt_name);
            size = itemView.findViewById(R.id.txt_size);
            type = itemView.findViewById(R.id.txt_type);
            path = itemView.findViewById(R.id.txt_path);
        }
    }
}
