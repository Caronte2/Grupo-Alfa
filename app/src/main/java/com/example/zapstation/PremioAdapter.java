package com.example.zapstation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PremioAdapter extends RecyclerView.Adapter<PremioAdapter.PremioViewHolder> {

    //Adaptador de premios
    private final List<Premio> premios;
    private final OnPremioCanjearListener listener;

    public interface OnPremioCanjearListener {
        void onCanjear(Premio premio);
    }

    public PremioAdapter(List<Premio> premios, OnPremioCanjearListener listener) {
        this.premios = premios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PremioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_premio, parent, false);
        return new PremioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PremioViewHolder holder, int position) {
        Premio premio = premios.get(position);

        // Configura el texto y la imagen
        holder.tvNombrePremio.setText(premio.getNombre());
        holder.tvPuntosPremio.setText(premio.getPuntos() + " puntos");
        holder.ivPremio.setImageResource(premio.getRecursoImagen());

        // Maneja el clic del botón "Canjear"
        holder.btnCanjear.setOnClickListener(v -> {
            // Ejecuta el callback que maneja el evento
            listener.onCanjear(premio);
        });
    }



    @Override
    public int getItemCount() {
        return premios.size();
    }

    static class PremioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePremio, tvPuntosPremio;
        ImageView ivPremio;
        Button btnCanjear;

        public PremioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePremio = itemView.findViewById(R.id.tvNombrePremio);
            tvPuntosPremio = itemView.findViewById(R.id.tvPuntosPremio);
            ivPremio = itemView.findViewById(R.id.ivPremio);
            btnCanjear = itemView.findViewById(R.id.btnCanjear);
        }
    }
}
