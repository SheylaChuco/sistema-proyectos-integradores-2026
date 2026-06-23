from rest_framework import serializers

from apps.proyectos.models import Propuesta, Grupo


class IntegranteResumenSerializer(serializers.Serializer):
    nombre = serializers.SerializerMethodField()
    codigo_estudiante = serializers.SerializerMethodField()

    def get_nombre(self, obj):
        return obj.estudiante.usuario.nombre

    def get_codigo_estudiante(self, obj):
        return obj.estudiante.codigo_estudiante


class GrupoResumenSerializer(serializers.ModelSerializer):
    integrantes = IntegranteResumenSerializer(many=True, read_only=True)

    class Meta:
        model = Grupo
        fields = ['id', 'codigo_grupo', 'periodo', 'integrantes']


class PropuestaListaSerializer(serializers.ModelSerializer):
    grupo_codigo = serializers.CharField(source='grupo.codigo_grupo', read_only=True)
    grupo_periodo = serializers.CharField(source='grupo.periodo', read_only=True)

    class Meta:
        model = Propuesta
        fields = [
            'id', 'nombre', 'descripcion', 'estado',
            'comentario_observacion', 'fecha_envio',
            'grupo_codigo', 'grupo_periodo',
        ]


class PropuestaDetalleSerializer(serializers.ModelSerializer):
    grupo = GrupoResumenSerializer(read_only=True)

    class Meta:
        model = Propuesta
        fields = [
            'id', 'nombre', 'descripcion', 'estado',
            'comentario_observacion', 'fecha_envio', 'grupo',
        ]
