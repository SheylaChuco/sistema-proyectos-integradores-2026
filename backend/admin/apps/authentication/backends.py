from rest_framework import authentication, exceptions
from rest_framework_simplejwt.exceptions import TokenError
from rest_framework_simplejwt.settings import api_settings
from rest_framework_simplejwt.tokens import AccessToken


class _UsuarioAutenticado:
    """Objeto mínimo que satisface la verificación is_authenticated de DRF sin depender de AUTH_USER_MODEL."""
    is_authenticated = True
    is_active = True

    def __init__(self, usuario_id, correo, rol):
        self.pk = usuario_id
        self.correo = correo
        self.rol = rol


class JwtTokenAuthentication(authentication.BaseAuthentication):
    """Valida el JWT usando nuestra clave sin delegar en AUTH_USER_MODEL de Django."""

    def authenticate(self, request):
        header = authentication.get_authorization_header(request).split()
        if not header or header[0].lower() != b'bearer':
            return None
        if len(header) != 2:
            raise exceptions.AuthenticationFailed('Token malformado.')
        try:
            token = AccessToken(header[1].decode('utf-8'))
        except TokenError as e:
            raise exceptions.AuthenticationFailed(str(e))

        usuario_id = token.get(api_settings.USER_ID_CLAIM)
        correo = token.get('correo', '')
        rol = token.get('rol', '')
        return (_UsuarioAutenticado(usuario_id, correo, rol), token)

    def authenticate_header(self, request):
        return 'Bearer realm="api"'
