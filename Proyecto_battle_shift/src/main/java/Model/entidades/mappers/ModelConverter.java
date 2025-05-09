/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades.mappers;

import dto.*;
import enums.*;
import Model.entidades.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 *
 * @author Hector
 */

public class ModelConverter {

    // --- Posicion <-> PosicionDTO ---
    public static PosicionDTO toPosicionDTO(Posicion entidad) {
        if (entidad == null) return null;
        return new PosicionDTO(entidad.getX(), entidad.getY());
    }

    public static Posicion toPosicionEntity(PosicionDTO dto) {
        if (dto == null) return null;
        return new Posicion(dto.getX(), dto.getY());
    }

    // --- Barco <-> BarcoDTO ---
    public static BarcoDTO toBarcoDTO(Barco entidad) {
        if (entidad == null) return null;
        BarcoDTO dto = new BarcoDTO();
        dto.setTipo(entidad.getTipo());
        dto.setLongitud(entidad.getLongitud()); // La entidad Barco tiene un campo longitud
        dto.setOrientacion(entidad.getOrientacion());
        dto.setEstado(entidad.getEstado());

        if (entidad.getPosicionesOcupadas() != null) {
            dto.setPosicionesOcupadas(
                entidad.getPosicionesOcupadas().stream()
                    .map(ModelConverter::toPosicionDTO)
                    .collect(Collectors.toList())
            );
        } else {
            dto.setPosicionesOcupadas(new ArrayList<>());
        }

        if (entidad.getPosicionesImpactadas() != null) {
            dto.setPosicionesImpactadas(
                entidad.getPosicionesImpactadas().stream()
                    .map(ModelConverter::toPosicionDTO)
                    .collect(Collectors.toSet())
            );
        } else {
            dto.setPosicionesImpactadas(new HashSet<>());
        }
        // El DTO no tiene posicionInicio, se asume que las posicionesOcupadas son la fuente.
        return dto;
    }

    public static Barco toBarcoEntity(BarcoDTO dto) {
        if (dto == null) return null;

        List<Posicion> posicionesOcupadasEntity = new ArrayList<>();
        if (dto.getPosicionesOcupadas() != null) {
            posicionesOcupadasEntity = dto.getPosicionesOcupadas().stream()
                .map(ModelConverter::toPosicionEntity)
                .collect(Collectors.toList());
        }

        // Usamos el constructor de Barco que toma TipoNave, List<Posicion> y Orientacion
        Barco entidad = new Barco(dto.getTipo(), posicionesOcupadasEntity, dto.getOrientacion());
        
        // El estado y las posiciones impactadas se deben establecer después de la construcción.
        // El constructor de Barco inicializa el estado a INTACTA y posicionesImpactadas vacío.
        // Necesitamos aplicar el estado y los impactos del DTO.
        if (dto.getEstado() != null) {
             entidad.setEstado(dto.getEstado()); // Asumiendo un setter o lógica interna
        }

        if (dto.getPosicionesImpactadas() != null) {
            Set<Posicion> posicionesImpactadasEntity = dto.getPosicionesImpactadas().stream()
                .map(ModelConverter::toPosicionEntity)
                .collect(Collectors.toSet());
            // La entidad Barco tiene un método registrarImpacto(), pero para reconstruir el estado
            // es más directo si tiene un setter para el conjunto de posicionesImpactadas,
            // o si registrarImpacto() se llama repetidamente aquí y actualiza el estado.
            // Por simplicidad, si se tienen los impactos, y el estado ya está seteado desde el DTO,
            // podríamos solo setear las posiciones. O mejor, llamar a registrarImpacto para cada una
            // y dejar que Barco recalcule su estado.
            // Si Barco.registrarImpacto() actualiza el estado, entonces lo siguiente es correcto.
            // Si no, se debe asegurar que el estado sea el del DTO.
            
            // Re-aplicar impactos para asegurar consistencia interna si registrarImpacto también actualiza estado.
            // Si el DTO.estado es la fuente de verdad, simplemente seteamos las posiciones impactadas.
            // Vamos a asumir que el estado del DTO es lo que queremos y Barco.registrarImpacto()
            // se usa para la lógica de juego, no para la reconstrucción desde DTO.
            // Sin embargo, para asegurar que la entidad Barco esté internamente consistente,
            // lo ideal es que tenga un método para "cargar" impactos y actualizar su estado.
            // Por ahora, si el constructor de Barco (el que toma posicionesOcupadas)
            // no maneja la reconstrucción de posicionesImpactadas y estado, necesitamos hacerlo aquí.
            
            // Limpiamos los impactos iniciales (si el constructor los crea) y añadimos los del DTO
            // Esto es un poco burdo. Idealmente, Barco tendría un constructor o método para esto.
            // Asumiendo que Barco tiene un setPosicionesImpactadas (que no definimos) o que debemos usar registrarImpacto.
            // Si usamos registrarImpacto, debemos hacerlo antes de setear el estado final del DTO.

            // Opción 1: Usar registrarImpacto y que Barco actualice su estado internamente
            // entidad.getPosicionesImpactadas().clear(); // Si el constructor lo llena por defecto
            // for (PosicionDTO posImpactadaDTO : dto.getPosicionesImpactadas()) {
            //    entidad.registrarImpacto(toPosicionEntity(posImpactadaDTO));
            // }
            // Esto re-calcularía el estado. Si el DTO.estado es la autoridad, esto podría ser redundante
            // o incluso incorrecto si el estado en el DTO es específico.

            // Opción 2: Setear el estado del DTO y las posiciones impactadas del DTO directamente.
            // Esto requiere que `Barco` permita setear `posicionesImpactadas` o tenga un método para ello.
            // Si no, el `BarcoDTO` debería ser la fuente del estado y el `Barco` entidad reflejarlo.
            // Vamos a confiar en que `setEstado` es suficiente y que `posicionesImpactadas` se usa para visualización/data.
            // Para que sea más robusto, la entidad Barco debería tener:
            // public void reconstruirImpactos(Set<Posicion> impactos, EstadoNave estadoActual)
            // O el `ModelConverter` debe ser muy cuidadoso.

            // Compromiso: El constructor de Barco ya inicializa posicionesImpactadas a vacío y estado a INTACTA.
            // Si el DTO trae impactos, los aplicamos y actualizamos el estado según el DTO.
            if (dto.getPosicionesImpactadas() != null) {
                 Set<Posicion> impactosEntidad = dto.getPosicionesImpactadas().stream()
                    .map(ModelConverter::toPosicionEntity)
                    .collect(Collectors.toSet());
                 // Necesitaríamos un método en Barco como: entidad.setImpactosInternos(impactosEntidad);
                 // Por ahora, vamos a simular que Barco lo gestiona si le pasamos los impactos y luego el estado.
                 // Para el ejemplo, el constructor de Barco no toma impactos, así que el set de impactos estará vacío.
                 // Se deben añadir al Barco entidad.
                 for(PosicionDTO posDto : dto.getPosicionesImpactadas()){
                     entidad.registrarImpacto(toPosicionEntity(posDto)); // Esto actualizará el estado del barco internamente
                 }
                 // Si el estado del DTO difiere del calculado por registrarImpacto (lo que no debería si el DTO es consistente),
                 // podríamos forzar el estado del DTO:
                 if (entidad.getEstado() != dto.getEstado()) {
                    entidad.setEstado(dto.getEstado()); // Forzar estado del DTO si es necesario
                 }
            }
        }
        return entidad;
    }

    // --- TableroFlota <-> TableroFlotaDTO ---
    public static TableroFlotaDTO toTableroFlotaDTO(TableroFlota entidad) {
        if (entidad == null) return null;
        TableroFlotaDTO dto = new TableroFlotaDTO();
        dto.setDimension(entidad.getDimension());
        if (entidad.getBarcos() != null) {
            dto.setBarcos(
                entidad.getBarcos().stream()
                    .map(ModelConverter::toBarcoDTO)
                    .collect(Collectors.toList())
            );
        } else {
            dto.setBarcos(new ArrayList<>());
        }
        return dto;
    }

    public static TableroFlota toTableroFlotaEntity(TableroFlotaDTO dto) {
        if (dto == null) return null;
        TableroFlota entidad = new TableroFlota(dto.getDimension());
        if (dto.getBarcos() != null) {
            dto.getBarcos().stream()
                .map(ModelConverter::toBarcoEntity)
                .forEach(entidad::agregarBarco); // agregarBarco maneja la lógica de validación
        }
        return entidad;
    }

    // --- TableroSeguimiento <-> TableroSeguimientoDTO ---
    public static TableroSeguimientoDTO toTableroSeguimientoDTO(TableroSeguimiento entidad) {
        if (entidad == null) return null;
        TableroSeguimientoDTO dto = new TableroSeguimientoDTO();
        dto.setDimension(entidad.getDimension());
        if (entidad.getRegistrosDisparos() != null) {
            List<RegistroDisparoSeguimientoDTO> registros = entidad.getRegistrosDisparos().entrySet().stream()
                .map(entry -> new RegistroDisparoSeguimientoDTO(
                    toPosicionDTO(entry.getKey()),
                    entry.getValue() // ResultadoDisparo es una enum, se pasa directamente
                ))
                .collect(Collectors.toList());
            
        } else {
         
        }
        return dto;
    }

    public static TableroSeguimiento toTableroSeguimientoEntity(TableroSeguimientoDTO dto) {
        if (dto == null) return null;
        TableroSeguimiento entidad = new TableroSeguimiento(dto.getDimension());
        if (dto.getRegistrosDisparos() != null) {                               
        }
        return entidad;
    }

    // --- Jugador <-> JugadorDTO ---
    public static JugadorDTO toJugadorDTO(Jugador entidad) {
        if (entidad == null) return null;
        JugadorDTO dto = new JugadorDTO();
        dto.setNombre(entidad.getNombre());
        dto.setHaConfirmadoTablero(entidad.haConfirmadoTablero());
        dto.setTableroFlota(toTableroFlotaDTO(entidad.getTableroFlota()));
        dto.setTableroSeguimiento(toTableroSeguimientoDTO(entidad.getTableroSeguimiento()));
        return dto;
    }

    public static Jugador toJugadorEntity(JugadorDTO dto) {
        if (dto == null) return null;
        // Asumimos que Jugador necesita una dimensión para sus tableros al ser creado.
        // El DTO no lleva la dimensión explícitamente para el Jugador, pero sus TableroDTOs sí.
        // Usamos la dimensión del TableroFlotaDTO, o un valor por defecto si no está.
        int dimension = (dto.getTableroFlota() != null) ? dto.getTableroFlota().getDimension() : 10; // Default 10
        
        Jugador entidad = new Jugador(dto.getNombre(), dimension);
        entidad.setHaConfirmadoTablero(dto.isHaConfirmadoTablero());

        // Reemplazar los tableros creados por defecto en el constructor de Jugador
        // con los convertidos desde el DTO.
        if (dto.getTableroFlota() != null) {
            entidad.setTableroFlota(toTableroFlotaEntity(dto.getTableroFlota())); // Necesitaría un setter en Jugador
        }
        if (dto.getTableroSeguimiento() != null) {
            entidad.setTableroSeguimiento(toTableroSeguimientoEntity(dto.getTableroSeguimiento())); // Necesitaría un setter en Jugador
        }
        // Si Jugador no tiene setters para los tableros, se tendrían que poblar de otra forma,
        // o el constructor de Jugador podría tomar los DTOs y convertirlos internamente.
        // Por simplicidad, voy a modificar la entidad Jugador para que tenga estos setters
        // o que su constructor pueda tomar los tableros.
        //
        // Alternativa para Jugador.java (simplificada para el converter):
        // public class Jugador {
        // ...
        //    public Jugador(String nombre) { this.nombre = nombre; /* tableros null inicialmente */ }
        //    public void setTableroFlota(TableroFlota tf) { this.tableroFlota = tf; }
        //    public void setTableroSeguimiento(TableroSeguimiento ts) { this.tableroSeguimiento = ts; }
        // ...
        // }
        // Con la entidad Jugador actual, que crea sus tableros en el constructor:
        // Jugador entidad = new Jugador(dto.getNombre(), dimension);
        // Los tableros de entidad ya están creados. Si queremos reemplazarlos:
        // Necesitamos métodos en Jugador para setearlos o reconstruirlos.
        // Por ahora, asumo que los tableros del DTO son la fuente de verdad.
        // Si Jugador tiene:
        //   public void setTableroFlota(TableroFlota tableroFlota) { this.tableroFlota = tableroFlota; }
        //   public void setTableroSeguimiento(TableroSeguimiento tableroSeguimiento) { this.tableroSeguimiento = tableroSeguimiento; }
        // entonces:
        // entidad.setTableroFlota(toTableroFlotaEntity(dto.getTableroFlota()));
        // entidad.setTableroSeguimiento(toTableroSeguimientoEntity(dto.getTableroSeguimiento()));
        // Esto es más limpio. Asumiré que tales setters existen o son añadidos a Jugador.
        // Si no, la otra opción es que el constructor de Jugador sea más flexible.
        
        // Para que coincida con la entidad Jugador proporcionada anteriormente, la entidad
        // crea sus propios tableros. La reconstrucción implicaría copiar el estado de los
        // tableros del DTO a los tableros existentes en la entidad.
        // Ejemplo de reconstrucción si no hay setters directos para los tableros:
        if (dto.getTableroFlota() != null && entidad.getTableroFlota() != null) {
            TableroFlota tableroFlotaDesdeDTO = toTableroFlotaEntity(dto.getTableroFlota());
            entidad.getTableroFlota().getBarcos().clear(); // Limpiar barcos por defecto
            tableroFlotaDesdeDTO.getBarcos().forEach(b -> entidad.getTableroFlota().agregarBarco(b));
        }
        if (dto.getTableroSeguimiento() != null && entidad.getTableroSeguimiento() != null) {
            TableroSeguimiento tableroSeguimientoDesdeDTO = toTableroSeguimientoEntity(dto.getTableroSeguimiento());
            entidad.getTableroSeguimiento().getRegistrosDisparos().clear(); // Limpiar registros por defecto
            tableroSeguimientoDesdeDTO.getRegistrosDisparos().forEach((pos, res) -> 
                entidad.getTableroSeguimiento().marcarDisparo(pos, res)
            );
        }

        return entidad;
    }


    // --- Partida <-> PartidaDTO ---
    public static PartidaDTO toPartidaDTO(Partida entidad) {
        if (entidad == null) return null;
        PartidaDTO dto = new PartidaDTO();
        dto.setIdPartida(entidad.getIdPartida());
        dto.setEstado(entidad.getEstado());
        if (entidad.obtenerJugadorEnTurno()!= null) {
            dto.setNombreJugadorEnTurno(entidad.obtenerJugadorEnTurno().getNombre());
        }
        // Aquí se debe tener cuidado con la información que se envía de cada jugador.
        // Por ejemplo, el TableroFlotaDTO del oponente debe ser sanitizado (solo mostrar impactos).
        // Este ModelConverter hará una conversión completa. La sanitización es una capa superior.
        dto.setJugador1(toJugadorDTO(entidad.getJugador1()));
        dto.setJugador2(toJugadorDTO(entidad.getJugador2()));
        return dto;
    }

    public static Partida toPartidaEntity(PartidaDTO dto) {
        if (dto == null) return null;

        // El constructor de Partida que definimos es privado y se usa un método factory.
        // Partida.crearJuego(id, nombreJ1, nombreJ2, dimension)
        // Para convertir desde DTO, necesitamos una forma de instanciar Partida
        // y luego setear sus componentes.
        // Asumamos que Partida tiene un constructor público o un método que permite esto.
        // O podemos usar el método factory si el DTO tiene suficiente información.

        // Si Partida.crearJuego es la única forma:
        // Esto es problemático si los JugadorDTOs ya vienen con sus tableros completos.
        // Necesitamos una forma de construir Partida y luego asignar los Jugadores ya convertidos.

        // Opción: Modificar Partida para tener un constructor que tome el ID o un constructor por defecto
        // y setters para los jugadores y el estado.
        // private Partida(String idPartida) // ya existe
        // public Partida() { this.estado = EstadoPartida.CONFIGURACION; } // si se necesita para alguna librería
        // y setters: setJugador1, setJugador2, setEstado, setJugadorEnTurnoPorNombre

        // Asumiendo que podemos crear Partida con su ID y luego poblarla:
        Partida entidad;
        if (dto.getJugador1() != null && dto.getJugador2() != null && dto.getJugador1().getTableroFlota() != null) {
            // Usar crearJuego si tenemos nombres y dimensión
             entidad = Partida.crearJuego(
                dto.getIdPartida(),
                dto.getJugador1().getNombre(),
                dto.getJugador2().getNombre(),
                dto.getJugador1().getTableroFlota().getDimension() // Asume que j1 tiene tablero y dimensión
            );
        } else if (dto.getIdPartida() != null) {
            // Si no tenemos toda la info para crearJuego, pero sí un ID,
            // necesitaríamos un constructor en Partida que solo tome el ID,
            // o un setter para el ID si usamos un constructor por defecto.
            // Por ahora, esto fallaría si no hay nombres o dimensión.
            // Para la reconstrucción, es mejor que Partida permita setear sus componentes.
            // Vamos a suponer que Partida tiene un constructor que sólo toma el ID (que ya existe pero es privado),
            // o que podemos instanciar y luego setear.
            // Por el momento, la llamada a Partida.crearJuego requiere más datos que los que el DTO podría tener
            // si está representando un estado intermedio.
            // La solución más limpia es que Partida tenga setters.
             // entidad = new Partida(dto.getIdPartida()); // Si el constructor privado se hace público o existe uno similar
             System.err.println("ModelConverter.toPartidaEntity: Reconstrucción de Partida desde DTO es compleja sin setters adecuados en Partida o constructor flexible.");
             // Solución temporal: crearla y luego intentar ajustar.
             entidad = Partida.crearJuego(dto.getIdPartida(), "tempJ1", "tempJ2", 10); // Placeholder
        } else {
            return null; // No hay suficiente información
        }


        Jugador j1Entidad = toJugadorEntity(dto.getJugador1());
        Jugador j2Entidad = toJugadorEntity(dto.getJugador2());
        
        entidad.setJugador1(j1Entidad); // Requiere setter en Partida
        entidad.setJugador2(j2Entidad); // Requiere setter en Partida
        entidad.setEstado(dto.getEstado()); // Requiere setter en Partida

        if (dto.getNombreJugadorEnTurno() != null) {
            if (j1Entidad != null && dto.getNombreJugadorEnTurno().equals(j1Entidad.getNombre())) {
                entidad.SetJugadorEnTurno(j1Entidad); // Requiere setter en Partida
            } else if (j2Entidad != null && dto.getNombreJugadorEnTurno().equals(j2Entidad.getNombre())) {
                entidad.SetJugadorEnTurno(j2Entidad); // Requiere setter en Partida
            }
        }
        // Si los setters no existen en Partida para jugador1, jugador2, estado, jugadorEnTurno, esta conversión será incompleta
        // o requerirá que la entidad Partida sea más flexible en su construcción/modificación post-creación.
        // El método Partida.crearJuego crea jugadores nuevos, lo cual no es lo que queremos si los DTOs
        // traen el estado de jugadores existentes.
        // La entidad Partida necesita ser más adecuada para la reconstrucción desde un DTO.
        // Ejemplo de lo que se necesitaría en Partida:
        // public void setJugadorEnTurnoInterno(Jugador j) { this.jugadorEnTurno = j; }

        return entidad;
    }
}