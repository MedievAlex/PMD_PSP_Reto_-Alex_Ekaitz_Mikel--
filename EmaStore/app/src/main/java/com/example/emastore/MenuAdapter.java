package com.example.emastore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private List<APK> apks;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(APK apk);
    }

    public MenuAdapter(List<APK> apks, OnItemClickListener listener) {
        this.apks = apks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        APK apk = apks.get(position);

        // Configurar los datos de la APK en las vistas
        holder.textViewTitle.setText(apk.getTitulo());

        // Mostrar autor (puedes usar otro TextView si lo necesitas)
        if (holder.textViewSubtitle != null) {
            holder.textViewSubtitle.setText(apk.getAutor());
        }

        // Mostrar descripción (opcional)
        if (holder.textViewDescription != null) {
            holder.textViewDescription.setText(apk.getDescripcion());
        }

        // Configurar imagen - puedes usar diferentes imágenes según el tipo
        // Por ahora uso una imagen genérica
        holder.imageViewIcon.setImageResource(R.drawable.ic_launcher_foreground);

        // Si tienes URLs de imágenes, aquí cargarías con Picasso/Glide
        /*
        if (apk.getImage() != null && !apk.getImage().isEmpty()) {
            Picasso.get().load(apk.getImage()).into(holder.imageViewIcon);
        }
        */
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(apk); // Pasar la APK completa
            }
        });
    }
    @Override
    public int getItemCount() {
        return apks.size();
    }
    public void setAPKs(List<APK> nuevasAPKs) {
        this.apks = nuevasAPKs;
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIcon;
        TextView textViewTitle;
        TextView textViewSubtitle;
        TextView textViewDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
        }
    }
}