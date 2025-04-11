package com.proyecto.ProyectoConectacare.model;


import java.util.List;


public class Anuncio {
    private String id;
    private String clienteId;

    private List<String> hogar;
    private List<String> personal;
    private List<String> acompanamiento;
    private List<String> salud;
    private String comentarios;

    public Anuncio() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public List<String> getHogar() {
        return hogar;
    }

    public void setHogar(List<String> hogar) {
        this.hogar = hogar;
    }

    public List<String> getPersonal() {
        return personal;
    }

    public void setPersonal(List<String> personal) {
        this.personal = personal;
    }

    public List<String> getAcompanamiento() {
        return acompanamiento;
    }

    public void setAcompanamiento(List<String> acompanamiento) {
        this.acompanamiento = acompanamiento;
    }

    public List<String> getSalud() {
        return salud;
    }

    public void setSalud(List<String> salud) {
        this.salud = salud;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
