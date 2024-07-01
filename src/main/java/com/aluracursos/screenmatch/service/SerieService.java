package com.aluracursos.screenmatch.service;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.dto.SerieDTO;
import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repository;

    public List<SerieDTO> obtenerTodasLasSeries() {
        return convierteDataos(repository.findAll());
    }

    public List<SerieDTO> obtenerTop5() {
        return convierteDataos(repository.findTop5ByOrderByEvaluacionDesc());
    }

    public List<SerieDTO> obtenerLanzamientosRecientes() {
        return convierteDataos(repository.lanzamientosMasRecientes());
    }

    public SerieDTO obtenerPorID(Long id) {
        Optional<Serie> serie = repository.findById(id);

        if (serie.isEmpty()) {
            return null;
        }

        Serie s = serie.get();
        return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getEvaluacion(), s.getPoster(), s.getGenero(), s.getActores(), s.getSinopsis());

    }

    public List<EpisodioDTO> obtenerTodasLasTemporadas(Long id) {
        Optional<Serie> serie = repository.findById(id);

        if(serie.isEmpty()) {
            return null;
        }

        Serie s = serie.get();
        return s.getEpisodios().stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
                .collect(Collectors.toList());
    }

    public List<EpisodioDTO> obtenerTopEpisodios(Long id) {
        Optional<Serie> serie = repository.findById(id);

        if(serie.isEmpty()) {
            return null;
        }

        Serie s = serie.get();
        return s.getEpisodios().stream()
                .sorted(Comparator.comparing(Episodio::getEvaluacion).reversed())
                .limit(5)
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
                .collect(Collectors.toList());
    }

    public List<EpisodioDTO> obtenerTemporadasPorNumero(Long id, Long numeroTemporada) {
        return repository.obtenerTemporadasPorNumero(id,numeroTemporada).stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
                .collect(Collectors.toList());
//        Optional<Serie> serie = repository.findById(id);
//
//        if(serie.isEmpty()) {
//            return null;
//        }
//
//        Serie s = serie.get();
//        return s.getEpisodios().stream()
//                .filter(e -> e.getTemporada().equals(temporada))
//                .map(e -> new EpisodioDTO(e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio()))
//                .collect(Collectors.toList());
    }

    public List<SerieDTO> obtenerSeriesPorCategoria(String nombreGenero) {
        Categoria categoria = Categoria.fromSpanol(nombreGenero);
        return convierteDataos(repository.findByGenero(categoria));
    }

    public List<SerieDTO> convierteDataos(List<Serie> serie) {
        return serie.stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getEvaluacion(), s.getPoster(), s.getGenero(), s.getActores(), s.getSinopsis()))
                .collect(Collectors.toList());
    }
}
