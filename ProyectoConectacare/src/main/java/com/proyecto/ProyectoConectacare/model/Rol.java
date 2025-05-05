package com.proyecto.ProyectoConectacare.model;

/**
 * Enum Rol define los roles asignados a los usuarios en la aplicación.
 *
 * - CLIENTE: Representa a un cliente dentro del sistema que puede crear anuncios de servicios.
 * - TRABAJADOR: Representa a un trabajador o proveedor de servicios que puede aplicar a los anuncios creados por los clientes.
 * - ADMINISTRADOR: Representa a un administrador con permisos elevados, responsable de la gestión del sistema.
 */
public enum Rol {
    CLIENTE, TRABAJADOR, ADMINISTRADOR
}
