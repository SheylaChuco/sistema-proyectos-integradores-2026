package pe.tecsup.proyectos.shared.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "grupo")
@Getter
@Setter
@NoArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_grupo", nullable = false, unique = true, length = 20)
    private String codigoGrupo;

    @Column(name = "periodo", nullable = false, length = 10)
    private String periodo;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private List<GrupoIntegrante> integrantes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
