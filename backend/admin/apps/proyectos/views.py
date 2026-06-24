from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.propuestas.permissions import EsAdmin
from .models import Proyecto
from .serializers import ProyectoSustentacionSerializer

_ERROR_NO_ENCONTRADO = {
    'success': False,
    'error': {'code': 'NO_ENCONTRADO', 'message': 'Proyecto no encontrado.'},
}

_QS = lambda: Proyecto.objects.filter(
    origen=Proyecto.Origen.NUEVO,
    estado=Proyecto.Estado.EN_DESARROLLO,
).select_related('grupo').order_by('-id')


class ListaProyectosSustentacionView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def get(self, request):
        serializer = ProyectoSustentacionSerializer(_QS(), many=True)
        return Response({'success': True, 'data': serializer.data})


class DetalleProyectoSustentacionView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def get(self, request, pk):
        try:
            proyecto = Proyecto.objects.select_related('grupo').get(
                pk=pk, origen=Proyecto.Origen.NUEVO
            )
        except Proyecto.DoesNotExist:
            return Response(_ERROR_NO_ENCONTRADO, status=status.HTTP_404_NOT_FOUND)
        serializer = ProyectoSustentacionSerializer(proyecto)
        return Response({'success': True, 'data': serializer.data})


class EvaluarProyectoView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    ESTADOS_VALIDOS = {Proyecto.Estado.APROBADO, Proyecto.Estado.NO_APROBADO}

    def put(self, request, pk):
        try:
            proyecto = Proyecto.objects.get(pk=pk, origen=Proyecto.Origen.NUEVO)
        except Proyecto.DoesNotExist:
            return Response(_ERROR_NO_ENCONTRADO, status=status.HTTP_404_NOT_FOUND)

        if proyecto.estado != Proyecto.Estado.EN_DESARROLLO:
            return Response(
                {
                    'success': False,
                    'error': {
                        'code': 'ESTADO_INVALIDO',
                        'message': 'Solo se pueden evaluar proyectos en estado EN_DESARROLLO.',
                    },
                },
                status=status.HTTP_422_UNPROCESSABLE_ENTITY,
            )

        nuevo_estado = request.data.get('estado', '').strip().upper()
        if nuevo_estado not in {e.value for e in self.ESTADOS_VALIDOS}:
            return Response(
                {
                    'success': False,
                    'error': {
                        'code': 'ESTADO_INVALIDO',
                        'message': 'El estado debe ser APROBADO o NO_APROBADO.',
                    },
                },
                status=status.HTTP_400_BAD_REQUEST,
            )

        proyecto.estado = nuevo_estado
        proyecto.comentario_evaluacion = request.data.get('comentario', '').strip() or None
        proyecto.save()

        return Response({
            'success': True,
            'data': {'id': proyecto.id, 'estado': proyecto.estado},
        })
