from django.contrib import admin
from django.urls import include, path

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/admin/auth/', include('apps.authentication.urls')),
    path('api/admin/propuestas/', include('apps.propuestas.urls')),
    path('api/admin/estadisticas', include('apps.estadisticas.urls')),
    path('api/admin/proyectos/', include('apps.proyectos.urls')),
    path('api/admin/estudiantes/', include('apps.estudiantes.urls')),
]
