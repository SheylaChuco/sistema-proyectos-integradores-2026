from rest_framework.permissions import BasePermission


class EsAdmin(BasePermission):
    def has_permission(self, request, view):
        return bool(request.auth and request.auth.get('rol') == 'ADMIN')
