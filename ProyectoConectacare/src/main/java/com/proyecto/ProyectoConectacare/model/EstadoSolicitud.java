package com.proyecto.ProyectoConectacare.model;

/**
 * La enumeración EstadoSolicitud representa los posibles estados de una solicitud de servicio.
 * Se utiliza para definir y gestionar el ciclo de vida y el estado actual de una solicitud específica.
 *
 * Estados:
 * - PENDIENTE: Indica que la solicitud está pendiente y a la espera de una acción o decisión.
 * - ACEPTADA: Indica que la solicitud ha sido aceptada.
 * - RECHAZADA: Indica que la solicitud ha sido rechazada.
 */
public enum EstadoSolicitud {
    PENDIENTE, ACEPTADA, RECHAZADA;


}
