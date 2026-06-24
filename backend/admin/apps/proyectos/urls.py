from django.urls import path
from .views import ListaProyectosSustentacionView, DetalleProyectoSustentacionView, EvaluarProyectoView

urlpatterns = [
    path('', ListaProyectosSustentacionView.as_view(), name='lista-proyectos-sustentacion'),
    path('<int:pk>', DetalleProyectoSustentacionView.as_view(), name='detalle-proyecto-sustentacion'),
    path('<int:pk>/evaluar', EvaluarProyectoView.as_view(), name='evaluar-proyecto'),
]
