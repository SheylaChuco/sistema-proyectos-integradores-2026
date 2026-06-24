from django.urls import path
from .views import EstadisticasView

urlpatterns = [
    path('', EstadisticasView.as_view(), name='estadisticas'),
]
