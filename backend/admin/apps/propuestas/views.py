from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.proyectos.models import Propuesta, Proyecto
from .permissions import EsAdmin
from .serializers import PropuestaListaSerializer, PropuestaDetalleSerializer

_ERROR_NO_ENCONTRADO = {
    'success': False,
    'error': {'code': 'NO_ENCONTRADO', 'message': 'Propuesta no encontrada.'},
}


class ListaPropuestasView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def get(self, request):
        estado = request.query_params.get('estado')
        qs = Propuesta.objects.select_related('grupo').prefetch_related(
            'grupo__integrantes__estudiante__usuario'
        ).order_by('-id')
        if estado:
            qs = qs.filter(estado=estado)
        serializer = PropuestaListaSerializer(qs, many=True)
        return Response({'success': True, 'data': serializer.data})


class AprobarPropuestaView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def put(self, request, pk):
        try:
            propuesta = Propuesta.objects.select_related('grupo').get(pk=pk)
        except Propuesta.DoesNotExist:
            return Response(_ERROR_NO_ENCONTRADO, status=status.HTTP_404_NOT_FOUND)

        propuesta.estado = Propuesta.Estado.APROBADO
        propuesta.save()

        # Crear Proyecto solo si no existe uno NUEVO para este grupo
        proyecto_existente = Proyecto.objects.filter(
            grupo=propuesta.grupo, origen=Proyecto.Origen.NUEVO
        ).first()

        if not proyecto_existente:
            proyecto = Proyecto.objects.create(
                origen=Proyecto.Origen.NUEVO,
                nombre=propuesta.nombre,
                descripcion=propuesta.descripcion,
                ciclo=propuesta.grupo.periodo,
                grupo=propuesta.grupo,
                estado=Proyecto.Estado.EN_DESARROLLO,
            )
            proyecto_id = proyecto.id
        else:
            proyecto_id = proyecto_existente.id

        return Response({
            'success': True,
            'data': {
                'id': propuesta.id,
                'estado': propuesta.estado,
                'proyecto_id': proyecto_id,
            },
        })


class ObservarPropuestaView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def put(self, request, pk):
        try:
            propuesta = Propuesta.objects.get(pk=pk)
        except Propuesta.DoesNotExist:
            return Response(_ERROR_NO_ENCONTRADO, status=status.HTTP_404_NOT_FOUND)

        comentario = request.data.get('comentario', '').strip()
        if not comentario:
            return Response(
                {
                    'success': False,
                    'error': {
                        'code': 'COMENTARIO_REQUERIDO',
                        'message': 'El comentario de observación es obligatorio.',
                    },
                },
                status=status.HTTP_400_BAD_REQUEST,
            )

        propuesta.estado = Propuesta.Estado.OBSERVADO
        propuesta.comentario_observacion = comentario
        propuesta.save()

        return Response({
            'success': True,
            'data': {'id': propuesta.id, 'estado': propuesta.estado},
        })
