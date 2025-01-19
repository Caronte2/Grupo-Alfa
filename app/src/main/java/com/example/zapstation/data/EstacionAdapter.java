package com.example.zapstation.data;

import android.util.Log;
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class EstacionAdapter extends FirestoreRecyclerAdapter<Estacion, EstacionAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public EstacionAdapter(@NonNull FirestoreRecyclerOptions<Estacion> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Estacion estacion) {
        if (position >= getItemCount()) {
            Log.e("EstacionAdapter", "Posición fuera de rango: " + position);
            return;
        }

        holder.tvNombre.setText(estacion.getNombre());
        holder.tvDireccion.setText(estacion.getDireccion());
        holder.ratingBar.setRating((float) estacion.getValoracion());
        holder.tvCoordenadas.setText(
                estacion.getPosicion() != null
                        ? "Lat: " + estacion.getPosicion().getLatitude() + ", Lng: " + estacion.getPosicion().getLongitude()
                        : "Coordenadas no disponibles"
        );

        if (estacion.getFoto() != null && !estacion.getFoto().isEmpty()) {
            loadImage(holder.ivFoto, estacion.getFoto());
        } else {
            holder.ivFoto.setImageResource(R.drawable.punto_carga);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estaciones, parent, false);
        return new ViewHolder(view, listener);
    }

    // Método para cargar la imagen de Firebase Storage
    public void loadImage(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .into(imageView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
