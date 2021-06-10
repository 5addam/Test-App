package com.testapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.testapp.R;
import com.testapp.models.StorageFile;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    List<StorageFile> storageFileList = new ArrayList<>();
    private OnItemClickListener listener;

    public FileAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<StorageFile> storageFiles) {
        this.storageFileList = storageFiles;
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
        StorageFile storageFile = storageFileList.get(position);
        holder.name.setText("Name: " + storageFile.getName());
        holder.size.setText("Size: " + String.valueOf(storageFile.getSize()));
        holder.path.setText("Path: " + storageFile.getPath());
        holder.type.setText(storageFile.getType());

    }

    @Override
    public int getItemCount() {
        if (storageFileList != null && storageFileList.size() >= 1)
            return storageFileList.size();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    if(listener != null && pos != RecyclerView.NO_POSITION)
                        listener.onItemClick(storageFileList.get(pos));
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(StorageFile storageFile);
    }
}
