package com.example.zapstation.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zapstation.R;
import com.example.zapstation.model.PrecioLuz;

import java.util.List;

public class PrecioLuzAdapter extends RecyclerView.Adapter<PrecioLuzAdapter.PrecioLuzViewHolder> {

    //Adaptador del precio de la luz
    private final List<PrecioLuz> precios;

    public PrecioLuzAdapter(List<PrecioLuz> precios) {
        this.precios = precios;
    }

    @NonNull
    @Override
    public PrecioLuzViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_precio_luz, parent, false);
        return new PrecioLuzViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrecioLuzViewHolder holder, int position) {
        PrecioLuz precioLuz = precios.get(position);

        // Configurar los textos
        holder.tvDiaSemana.setText(precioLuz.getDiaSemana());
        holder.tvPrecio.setText(precioLuz.getPrecioKWh() + " €/kWh");
        holder.tvHorario.setText("Horario: " + precioLuz.getHorario());
    }

    @Override
    public int getItemCount() {
        return precios.size();
    }

    static class PrecioLuzViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiaSemana, tvPrecio, tvHorario;

        public PrecioLuzViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiaSemana = itemView.findViewById(R.id.tvDiaSemana);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvHorario = itemView.findViewById(R.id.tvHorario);
        }
    }
}
