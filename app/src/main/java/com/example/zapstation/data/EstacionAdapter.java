package com.example.zapstation.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EstacionAdapter extends RecyclerView.Adapter<EstacionAdapter.ViewHolder> {
    private List<Estacion> estaciones;  // Lista de estaciones a mostrar
    private OnItemClickListener listener;  // Listener para manejar clics en los ítems

    public EstacionAdapter(List<Estacion> estaciones) {
        if (estaciones == null) {
            this.estaciones = new ArrayList<>();
        } else {
            this.estaciones = estaciones;
        }
    }

    public void setEstaciones(List<Estacion> estaciones) {
        this.estaciones = estaciones;
        notifyDataSetChanged();
    }

    // Establece el listener de los clics en los ítems
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño del ítem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estaciones, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener la estación de la posición actual
        Estacion estacion = estaciones.get(position);

        // Configurar los datos en los widgets
        holder.tvNombre.setText(estacion.getNombre());
        holder.tvDireccion.setText(estacion.getDireccion());
        holder.ratingBar.setRating((float) estacion.getValoracion());

        // Mostrar las coordenadas, con un chequeo de que no sea nulo
        GeoPoint posicion = estacion.getPosicion();
        if (posicion != null) {
            holder.tvCoordenadas.setText("Lat: " + posicion.getLatitude() + ", Lng: " + posicion.getLongitude());
        } else {
            holder.tvCoordenadas.setText("Coordenadas no disponibles");
        }

        // Cargar imagen si hay una URL válida
        if (estacion.getFoto() != null && !estacion.getFoto().isEmpty()) {
            loadImage(holder.ivFoto, estacion.getFoto());
        } else {
            holder.ivFoto.setImageResource(R.drawable.punto_carga); // Imagen por defecto
        }
    }


    @Override
    public int getItemCount() {
        return estaciones != null ? estaciones.size() : 0;
    }

    // Método para cargar la imagen desde Firebase Storage
    public void loadImage(ImageView imageView, String url) {
        if (url != null && url.startsWith("https://")) {
            // Cargar la imagen desde Firebase Storage si la URL es válida
            Glide.with(imageView.getContext())  // Usamos el contexto del ImageView
                    .load(url)       // La URL de la imagen en Firebase
                    .into(imageView); // ImageView en el que se carga la imagen
        } else {
            // Si la URL no es válida, usa una imagen predeterminada
            Glide.with(imageView.getContext())
                    .load(R.drawable.punto_carga)  // Reemplaza con tu imagen predeterminada
                    .into(imageView);
        }
    }


    // ViewHolder para optimizar el rendimiento del RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion, tvCoordenadas;
        ImageView ivFoto;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvCoordenadas = itemView.findViewById(R.id.tvCoordenadas);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            // Manejar clic en el ítem
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
    // Interfaz para manejar clics en los ítems
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

