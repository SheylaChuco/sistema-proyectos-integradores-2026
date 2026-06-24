from django.urls import path
from .views import ListaEstudiantesView

urlpatterns = [
    path('', ListaEstudiantesView.as_view()),
]
