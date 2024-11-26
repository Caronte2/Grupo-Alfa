package com.example.zapstation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AdaptadorEstaciones extends RecyclerView.Adapter<AdaptadorEstaciones.ViewHolder> {

    protected View.OnClickListener onClickListener;

    protected com.example.zapstation.RepositorioEstaciones estaciones; // Lista de estaciones a mostrar
    private int lugaresLimite = Integer.MAX_VALUE;

    public AdaptadorEstaciones(RepositorioEstaciones estaciones) {
        this.estaciones = estaciones;
    }

    // Máximo de lugares según las preferencias
    public void setEstaciones(int limit) {
        this.lugaresLimite = limit;
        notifyDataSetChanged(); // Actualizar la vista cuando se cambia el límite
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, direccion;
        public ImageView foto;
        public RatingBar valoracion;
        public TextView distancia;
        public ViewHolder(View itemView) {
            super(itemView);
            nombre = (TextView) itemView.findViewById(R.id.nombre);
            direccion = (TextView) itemView.findViewById(R.id.direccion);
            foto = (ImageView) itemView.findViewById(R.id.foto);
            valoracion = (RatingBar) itemView.findViewById(R.id.valoracion);
            distancia = itemView.findViewById(R.id.distancia);
        }
        // Personalizamos un ViewHolder a partir de un lugar
        public void personaliza(Estacion estacion) {
            nombre.setText(estacion.getNombre());
            direccion.setText(estacion.getDireccion());
            int id = R.drawable.otros;
            switch(estacion.getTipo()) {
                case RESTAURANTE:id = R.drawable.restaurante; break;
                case BAR: id = R.drawable.bar; break;
                case COPAS: id = R.drawable.copas; break;
                case ESPECTACULO:id = R.drawable.espectaculos; break;
                case HOTEL: id = R.drawable.hotel; break;
                case COMPRAS: id = R.drawable.compras; break;
                case EDUCACION: id = R.drawable.educacion; break;
                case DEPORTE: id = R.drawable.deporte; break;
                case NATURALEZA: id = R.drawable.naturaleza; break;
                case GASOLINERA: id = R.drawable.gasolinera; break; }
            foto.setImageResource(id);
            foto.setScaleType(ImageView.ScaleType.FIT_END);
            valoracion.setRating(estacion.getValoracion());
            GeoPunto pos=((Aplicacion) itemView.getContext().getApplicationContext())
                    .posicionActual;
            if (pos.equals(GeoPunto.SIN_POSICION) ||
                    estacion.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                distancia.setText("... Km");
            } else {
                int d=(int) pos.distancia(estacion.getPosicion());
                if (d < 2000) distancia.setText(d + " m");
                else distancia.setText(d / 1000 + " Km");
            }
        }
    }
    // Creamos el ViewHolder con la vista de un elemento sin personalizar
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflamos la vista desde el xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        view.setOnClickListener(onClickListener);
        return new ViewHolder(view);
    }
    // Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Estacion estacion = estaciones.elemento(posicion);
        holder.personaliza(estacion);
    }

    // Indicamos el número de elementos de la lista
    @Override
    public int getItemCount() {
        // Retorna el número limitado de lugares según el valor de lugaresLimit
        return Math.min(estaciones.tamaño(), lugaresLimite);
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
