from rest_framework import serializers
from .models import Proyecto, GrupoIntegrante


class IntegranteProyectoSerializer(serializers.Serializer):
    nombre = serializers.SerializerMethodField()
    codigo_estudiante = serializers.SerializerMethodField()

    def get_nombre(self, obj):
        return obj.estudiante.usuario.nombre

    def get_codigo_estudiante(self, obj):
        return obj.estudiante.codigo_estudiante


class ProyectoSustentacionSerializer(serializers.ModelSerializer):
    grupo_codigo = serializers.CharField(source='grupo.codigo_grupo', read_only=True)
    grupo_periodo = serializers.CharField(source='grupo.periodo', read_only=True)
    integrantes = serializers.SerializerMethodField()

    class Meta:
        model = Proyecto
        fields = [
            'id', 'nombre', 'descripcion', 'ciclo',
            'grupo_codigo', 'grupo_periodo', 'estado',
            'url', 'comentario_evaluacion', 'integrantes',
        ]

    def get_integrantes(self, obj):
        if not obj.grupo:
            return []
        qs = GrupoIntegrante.objects.filter(grupo=obj.grupo).select_related(
            'estudiante__usuario'
        )
        return IntegranteProyectoSerializer(qs, many=True).data
