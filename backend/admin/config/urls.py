from django.contrib import admin
from django.urls import include, path

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/admin/auth/', include('apps.authentication.urls')),
    path('api/admin/propuestas/', include('apps.propuestas.urls')),
    path('api/admin/estadisticas', include('apps.estadisticas.urls')),
]
