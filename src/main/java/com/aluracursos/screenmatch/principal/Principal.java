package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = System.getenv("ALU_OMDB_API_KEY");
    private ConvierteDatos conversor = new ConvierteDatos();
    //private List<DatosSerie> seriesBuscadas = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    Optional<List<Serie>> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Top 5
                    6 - Buscar por categoria
                    7 - Burcar por temporada y evaluacion
                    8 - Buscar episodios por nombre
                    9 - Top 5 episodios de serie
                    
                    0 - Salir""";
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;

                case 5:
                    buscarTop5();
                    break;

                case 6:
                    buscarPorCategoria();
                    break;
                case 7:
                    buscarTemporadaEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorNombre();
                    break;
                case 9:
                    burcarTop5Episodios();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie: ");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();

            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }
    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //seriesBuscadas.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriesPorTitulo() {
        System.out.println("Escribe el nombre de la serie: ");
        var nombreSerie = teclado.nextLine();

        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nombreSerie);
        if (serieBuscada.isEmpty()) {
            System.out.println("Serie no encontrada");
            return;
        }

        serieBuscada.get().forEach(System.out::println);
    }

    private void buscarTop5() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();

        topSeries.forEach(s -> System.out.println("* Serie: " + s.getTitulo() + "\n  Evaluacion: " + s.getEvaluacion() + "\n"));
    }

    private void buscarPorCategoria() {
        System.out.println("Escribe la categoria deseada: ");
        var categoriaBuscada = teclado.nextLine();

        var categoria = Categoria.fromSpanol(categoriaBuscada);

        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series de la categoria: " + categoriaBuscada);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarTemporadaEvaluacion() {
        System.out.println("Escribe el numero de temporadas: ");
        var temporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Escribe la evaluacion: ");
        var evaluacion = teclado.nextDouble();

        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvaluacion(temporadas, evaluacion);
        System.out.println("*** Filtrados ***");
        filtroSeries.forEach(s ->
                        System.out.println(s.getTitulo() + " - evaluacion: "+ s.getEvaluacion()));
    }

    private void buscarEpisodiosPorNombre() {
        System.out.println("Introduce el nombre del episodio ");
        var episodio = teclado.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(episodio);

        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s \nTemporada %s Episodio %s: %s \nEvaluacion: %s\n\n",e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getEvaluacion()));
    }

    private void burcarTop5Episodios() {
        buscarSeriesPorTitulo();

        if (serieBuscada.isPresent()) {
            List<Serie> series = serieBuscada.get();

            List<Episodio> topEpisodios = repositorio.top5Episodios(series.get(0));

            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s \nTemporada %s Episodio %s: %s \nEvaluacion: %s\n\n",e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getEvaluacion()));;
        }
    }
}

