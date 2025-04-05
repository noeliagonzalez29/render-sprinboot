package com.proyecto.ProyectoConectacare.controllers;

import com.proyecto.ProyectoConectacare.model.Evaluacion;
import com.proyecto.ProyectoConectacare.service.EvaluacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/evaluaciones")
public class EvaluacionController {
    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @PostMapping
    public ResponseEntity<Evaluacion> crearEvaluacion(@RequestBody Evaluacion evaluacion) {
        //evaluacion.setFechaEvaluacion(new Date()); quitado porque tengo anotacion de firestore para que lo maneje
        return new ResponseEntity<>(evaluacionService.createEvaluacion(evaluacion), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public Evaluacion obtenerEvaluacion(@PathVariable String id) {
        return evaluacionService.getEvaluacionById(id);
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public List<Evaluacion> obtenerEvaluacionesTrabajador(@PathVariable String trabajadorId) {
        return evaluacionService.getEvaluacionesByTrabajadorId(trabajadorId);
    }
}
