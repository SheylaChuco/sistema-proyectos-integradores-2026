from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.proyectos.models import Estudiante
from apps.propuestas.permissions import EsAdmin
from .serializers import EstudianteAdminSerializer

_QS = lambda: (
    Estudiante.objects
    .select_related('usuario')
    .prefetch_related('grupos__grupo')
    .order_by('usuario__nombre')
)


class ListaEstudiantesView(APIView):
    permission_classes = [IsAuthenticated, EsAdmin]

    def get(self, request):
        serializer = EstudianteAdminSerializer(_QS(), many=True)
        return Response({'success': True, 'data': serializer.data})
