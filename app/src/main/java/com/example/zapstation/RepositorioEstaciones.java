package com.example.zapstation;

public interface RepositorioEstaciones{
    Estacion elemento(int id); //Devuelve el elemento dado su id
    void añade(Estacion estacion); //Añade el elemento indicado
    int nuevo(); //Añade un elemento en blanco y devuelve su id
    void borrar(int id); //Elimina el elemento con el id indicado
    int tamaño(); //Devuelve el número de elementos
    void actualiza(int id, Estacion estacion); //Reemplaza un elemento
}
