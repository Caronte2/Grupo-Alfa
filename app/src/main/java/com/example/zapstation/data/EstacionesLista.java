package com.example.zapstation.data;

import com.example.zapstation.model.Estacion;
import com.example.zapstation.R;
import com.example.zapstation.model.TipoEstacion;

import java.util.ArrayList;
import java.util.List;

public class EstacionesLista implements RepositorioEstaciones {
    protected List<Estacion> listaEstaciones ;//= añadeEjemplos();//ejemploLugares();

    public EstacionesLista() {
        //listaLugares = ejemploLugares();
        listaEstaciones = new ArrayList<Estacion>();
        añadeEjemplos();
    }

    public Estacion elemento(int id) {
        return listaEstaciones.get(id);
    }

    public void añade(Estacion estacion) {
        listaEstaciones.add(estacion);
    }

    public int nuevo() {
        Estacion estacion = new Estacion();
        listaEstaciones.add(estacion);
        return listaEstaciones.size()-1;
    }

    public void borrar(int id) {
        listaEstaciones.remove(id);
    }

    public int tamaño() {
        return listaEstaciones.size();
    }
    public void actualiza(int id, Estacion estacion) {
        listaEstaciones.set(id, estacion);
    }

    //Lista de estaciones, TODO: Hacerlo en Firestore
    public void añadeEjemplos() {
        añade(new Estacion("Ajuntament de Sueca",
                "Plaça de l'Ajuntament, 10, 46410 Sueca, València",
                -0.310510, 39.202553, TipoEstacion.HOTEL, 644307085, "https://www.sueca.es/",
                "Mal lloc pa carregar el coche.", 1, R.drawable.foto_epsg));

        añade(new Estacion("Plaça de l'estació",
                "Parque de la estación, 46410 Sueca, Valencia",
                -0.308471, 39.205706, TipoEstacion.HOTEL, 644306095, "https://www.sueca.es/",
                "Ñenfe Cercanias.", 4, R.drawable.punto_carga));

        añade(new Estacion("Escuela Politécnica Superior de Gandía",
                "C/ Paranimf, 1 46730 Gandia (SPAIN)",
                -0.166093, 38.995656, TipoEstacion.HOTEL, 962849300, "http://www.epsg.upv.es",
                "Uno de los mejores lugares para formarse.", 5, R.drawable.cochecargando));
    }

        /*public static List<Lugar> añadeEjemplos2() {
        ArrayList<Lugar> lugares = new ArrayList<Lugar>();
        lugares.add(new Lugar("Escuela Politécnica Superior de Gandía",
                "C/ Paranimf, 1 46730 Gandia (SPAIN)",-0.166093,38.995656,
                TipoLugar.EDUCACION,962849300, "http://www.epsg.upv.es",
                "Uno de los mejores lugares para formarse.", 3));
        lugares.add(new Lugar("Al de siempre",
                "P.Industrial Junto Molí Nou - 46722, Benifla (Valencia)",
                -0.190642, 38.925857, TipoLugar.BAR, 636472405, "",
                "No te pierdas el arroz en calabaza.", 3));
        lugares.add(new Lugar("androidcurso.com",
                "ciberespacio", 0.0, 0.0, TipoLugar.EDUCACION,
                962849300, "http://androidcurso.com",
                "Amplia tus conocimientos sobre Android.", 5));
        lugares.add(new Lugar("Barranco del Infierno",
                "Vía Verde del río Serpis. Villalonga (Valencia)",
                -0.295058, 38.867180, TipoLugar.NATURALEZA, 0,
                "http://sosegaos.blogspot.com.es/2009/02/lorcha-villalonga-via-"+
                        "verde-del-rio.html","Espectacular ruta para bici o andar", 4));
        lugares.add(new Lugar("La Vital",
                "Avda. de La Vital, 0 46701 Gandía (Valencia)", -0.1720092,
                38.9705949, TipoLugar.COMPRAS, 962881070,
                "http://www.lavital.es/", "El típico centro comercial", 2));
        return lugares;
    }*/
}
