from django.db import models


class Usuario(models.Model):
    class Rol(models.TextChoices):
        ESTUDIANTE = 'ESTUDIANTE'
        ADMIN = 'ADMIN'

    nombre = models.CharField(max_length=255)
    correo = models.CharField(max_length=255, unique=True)
    password = models.CharField(max_length=255)
    rol = models.CharField(max_length=20, choices=Rol.choices)
    activo = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'usuario'

    def __str__(self):
        return f"{self.nombre} ({self.correo})"


class Estudiante(models.Model):
    usuario = models.OneToOneField(Usuario, on_delete=models.CASCADE,
                                   related_name='estudiante')
    codigo_estudiante = models.CharField(max_length=20, unique=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'estudiante'

    def __str__(self):
        return self.codigo_estudiante


class Administrador(models.Model):
    usuario = models.OneToOneField(Usuario, on_delete=models.CASCADE,
                                   related_name='administrador')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'administrador'

    def __str__(self):
        return str(self.usuario)


class Grupo(models.Model):
    codigo_grupo = models.CharField(max_length=20, unique=True)
    periodo = models.CharField(max_length=10)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'grupo'

    def __str__(self):
        return f"{self.codigo_grupo} ({self.periodo})"


class GrupoIntegrante(models.Model):
    grupo = models.ForeignKey(Grupo, on_delete=models.CASCADE,
                              related_name='integrantes')
    estudiante = models.ForeignKey(Estudiante, on_delete=models.CASCADE,
                                   related_name='grupos')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'grupo_integrante'
        unique_together = ('grupo', 'estudiante')

    def __str__(self):
        return f"{self.grupo} - {self.estudiante}"


class Propuesta(models.Model):
    class Estado(models.TextChoices):
        PENDIENTE = 'PENDIENTE'
        APROBADO = 'APROBADO'
        OBSERVADO = 'OBSERVADO'

    grupo = models.OneToOneField(Grupo, on_delete=models.CASCADE,
                                  related_name='propuesta')
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField()
    estado = models.CharField(max_length=20, choices=Estado.choices,
                              default=Estado.PENDIENTE)
    comentario_observacion = models.TextField(null=True, blank=True)
    fecha_envio = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'propuesta'

    def __str__(self):
        return f"{self.nombre} ({self.estado})"


class PropuestaVersion(models.Model):
    propuesta = models.ForeignKey(Propuesta, on_delete=models.CASCADE,
                                   related_name='versiones')
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField()
    version_fecha = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'propuesta_version'

    def __str__(self):
        return f"v{self.id} de {self.propuesta}"


class Proyecto(models.Model):
    class Origen(models.TextChoices):
        HISTORICO = 'HISTORICO'
        NUEVO = 'NUEVO'

    class Estado(models.TextChoices):
        EN_DESARROLLO = 'EN_DESARROLLO'
        APROBADO = 'APROBADO'
        NO_APROBADO = 'NO_APROBADO'

    origen = models.CharField(max_length=20, choices=Origen.choices)
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField()
    ciclo = models.CharField(max_length=10)
    codigo_seccion = models.CharField(max_length=20, null=True, blank=True)
    grupo = models.ForeignKey(Grupo, on_delete=models.SET_NULL,
                               null=True, blank=True, related_name='proyectos')
    estado = models.CharField(max_length=20, choices=Estado.choices,
                              null=True, blank=True)
    comentario_evaluacion = models.TextField(null=True, blank=True)
    url = models.CharField(max_length=500, null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'proyecto'

    def __str__(self):
        return f"{self.nombre} ({self.ciclo})"
