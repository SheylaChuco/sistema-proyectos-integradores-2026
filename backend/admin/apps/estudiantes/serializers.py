from rest_framework import serializers
from apps.proyectos.models import Estudiante


class EstudianteAdminSerializer(serializers.ModelSerializer):
    nombre = serializers.CharField(source='usuario.nombre')
    correo = serializers.CharField(source='usuario.correo')
    en_grupo = serializers.SerializerMethodField()
    grupo_codigo = serializers.SerializerMethodField()
    grupo_periodo = serializers.SerializerMethodField()

    class Meta:
        model = Estudiante
        fields = ['id', 'codigo_estudiante', 'nombre', 'correo',
                  'en_grupo', 'grupo_codigo', 'grupo_periodo']

    def _ultimo_gi(self, obj):
        grupos = list(obj.grupos.all())
        return max(grupos, key=lambda gi: gi.id) if grupos else None

    def get_en_grupo(self, obj):
        return bool(obj.grupos.all())

    def get_grupo_codigo(self, obj):
        gi = self._ultimo_gi(obj)
        return gi.grupo.codigo_grupo if gi else None

    def get_grupo_periodo(self, obj):
        gi = self._ultimo_gi(obj)
        return gi.grupo.periodo if gi else None
