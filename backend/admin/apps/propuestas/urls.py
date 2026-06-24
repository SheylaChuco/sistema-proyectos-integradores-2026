from django.urls import path

from .views import AprobarPropuestaView, DetallePropuestaView, ListaPropuestasView, ObservarPropuestaView

urlpatterns = [
    path('', ListaPropuestasView.as_view(), name='lista-propuestas'),
    path('<int:pk>', DetallePropuestaView.as_view(), name='detalle-propuesta'),
    path('<int:pk>/aprobar', AprobarPropuestaView.as_view(), name='aprobar-propuesta'),
    path('<int:pk>/observar', ObservarPropuestaView.as_view(), name='observar-propuesta'),
]
