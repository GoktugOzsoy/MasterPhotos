package com.example.masterphotos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.StorageViewHolder> {

    private List<String> imageURLs;
    private OnImageClickListener onImageClickListener;

    public StorageAdapter(List<String> imageURLs, OnImageClickListener onImageClickListener) {
        this.imageURLs = imageURLs;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public StorageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage, parent, false);
        return new StorageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StorageViewHolder holder, int position) {
        String imageURL = imageURLs.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageURL)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onImageClickListener.onImageClick(imageURL);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageURLs.size();
    }

    static class StorageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        StorageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewStorage);
        }
    }

    public interface OnImageClickListener {
        void onImageClick(String imageURL);
    }
}
