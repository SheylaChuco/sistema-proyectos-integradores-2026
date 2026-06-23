from django.contrib.auth.hashers import check_password
from rest_framework import status
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.settings import api_settings
from rest_framework_simplejwt.tokens import AccessToken

from apps.proyectos.models import Usuario

from .serializers import LoginSerializer

_ERROR_CREDENCIALES = {
    'success': False,
    'error': {
        'code': 'CREDENCIALES_INCORRECTAS',
        'message': 'Correo o contrasena incorrectos.',
    },
}


class LoginAdminView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = LoginSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(_ERROR_CREDENCIALES, status=status.HTTP_401_UNAUTHORIZED)

        correo = serializer.validated_data['correo']
        password = serializer.validated_data['password']

        try:
            usuario = Usuario.objects.get(
                correo=correo,
                rol=Usuario.Rol.ADMIN,
                activo=True,
            )
        except Usuario.DoesNotExist:
            return Response(_ERROR_CREDENCIALES, status=status.HTTP_401_UNAUTHORIZED)

        if not check_password(password, usuario.password):
            return Response(_ERROR_CREDENCIALES, status=status.HTTP_401_UNAUTHORIZED)

        token = AccessToken()
        token[api_settings.USER_ID_CLAIM] = usuario.id
        token['correo'] = usuario.correo
        token['nombre'] = usuario.nombre
        token['rol'] = 'ADMIN'

        return Response({
            'success': True,
            'data': {
                'access_token': str(token),
                'usuario': {
                    'id': usuario.id,
                    'nombre': usuario.nombre,
                    'correo': usuario.correo,
                    'rol': usuario.rol,
                },
            },
        }, status=status.HTTP_200_OK)
