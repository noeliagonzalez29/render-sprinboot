package com.proyecto.ProyectoConectacare.service;

public interface AuditService {

  void registrarAccion(String usuarioId, String accion, String detalle);
}
