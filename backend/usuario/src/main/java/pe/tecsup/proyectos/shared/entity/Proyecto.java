package pe.tecsup.proyectos.shared.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origen", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Origen origen;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ciclo", nullable = false, length = 10)
    private String ciclo;

    @Column(name = "codigo_seccion", length = 20)
    private String codigoSeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Column(name = "estado", length = 20)
    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Column(name = "comentario_evaluacion", columnDefinition = "TEXT")
    private String comentarioEvaluacion;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public enum Origen { HISTORICO, NUEVO }

    public enum Estado { EN_DESARROLLO, APROBADO, NO_APROBADO }
}
