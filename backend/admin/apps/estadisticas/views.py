from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.proyectos.models import Proyecto, Propuesta
from apps.propuestas.permissions import EsAdmin


class EstadisticasView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def get(self, request):
        total = Proyecto.objects.count()
        historicos = Proyecto.objects.filter(origen=Proyecto.Origen.HISTORICO).count()
        nuevos = Proyecto.objects.filter(origen=Proyecto.Origen.NUEVO).count()

        pendientes = Propuesta.objects.filter(estado=Propuesta.Estado.PENDIENTE).count()
        aprobadas = Propuesta.objects.filter(estado=Propuesta.Estado.APROBADO).count()
        observadas = Propuesta.objects.filter(estado=Propuesta.Estado.OBSERVADO).count()

        en_desarrollo = Proyecto.objects.filter(
            origen=Proyecto.Origen.NUEVO, estado=Proyecto.Estado.EN_DESARROLLO
        ).count()
        aprobados = Proyecto.objects.filter(
            origen=Proyecto.Origen.NUEVO, estado=Proyecto.Estado.APROBADO
        ).count()
        no_aprobados = Proyecto.objects.filter(
            origen=Proyecto.Origen.NUEVO, estado=Proyecto.Estado.NO_APROBADO
        ).count()

        from django.db.models import Count
        por_ciclo = (
            Proyecto.objects
            .values('ciclo')
            .annotate(total=Count('id'))
            .order_by('ciclo')
        )

        return Response({
            'success': True,
            'data': {
                'total_proyectos': total,
                'proyectos_historicos': historicos,
                'proyectos_nuevos': nuevos,
                'propuestas_pendientes': pendientes,
                'propuestas_aprobadas': aprobadas,
                'propuestas_observadas': observadas,
                'proyectos_en_desarrollo': en_desarrollo,
                'proyectos_aprobados': aprobados,
                'proyectos_no_aprobados': no_aprobados,
                'por_ciclo': list(por_ciclo),
            },
        })
