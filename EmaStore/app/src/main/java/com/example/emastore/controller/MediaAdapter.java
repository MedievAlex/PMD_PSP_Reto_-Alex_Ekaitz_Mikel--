package com.example.emastore.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.emastore.R;
import com.example.emastore.model.MediaItem;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private List<MediaItem> mediaList;

    public MediaAdapter(List<MediaItem> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);

        if (item.getType() == MediaItem.TYPE_IMAGE) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setImageResource(item.getResourceId());
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);

            String videoPath = "android.resource://" +
                    holder.itemView.getContext().getPackageName() + "/" + item.getResourceId();
            holder.videoView.setVideoPath(videoPath);

            holder.videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                holder.videoView.start();
            });
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public void pausarVideoEnPosicion(int position) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
        }

        public void pausarVideo() {
            if (videoView.isPlaying()) {
                videoView.pause();
            }
        }

        public boolean tieneVideo() {
            return videoView.getVisibility() == View.VISIBLE;
        }
    }
}