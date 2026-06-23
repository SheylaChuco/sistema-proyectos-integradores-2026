import pytest
from django.contrib.auth.hashers import make_password
from django.test import Client

from apps.proyectos.models import Administrador, Usuario

URL_LOGIN = '/api/admin/auth/login'


@pytest.fixture
def admin_user(db):
    usuario = Usuario.objects.create(
        nombre='Admin Test',
        correo='admin_test@tecsup.edu.pe',
        password=make_password('passwordseguro'),
        rol=Usuario.Rol.ADMIN,
        activo=True,
    )
    Administrador.objects.create(usuario=usuario)
    return usuario


@pytest.mark.django_db
def test_login_exitoso(client: Client, admin_user):
    response = client.post(
        URL_LOGIN,
        {'correo': 'admin_test@tecsup.edu.pe', 'password': 'passwordseguro'},
        content_type='application/json',
    )
    assert response.status_code == 200
    data = response.json()
    assert data['success'] is True
    assert 'access_token' in data['data']
    assert data['data']['usuario']['rol'] == 'ADMIN'
    assert data['data']['usuario']['correo'] == 'admin_test@tecsup.edu.pe'


@pytest.mark.django_db
def test_login_password_incorrecto(client: Client, admin_user):
    response = client.post(
        URL_LOGIN,
        {'correo': 'admin_test@tecsup.edu.pe', 'password': 'passworderroneo'},
        content_type='application/json',
    )
    assert response.status_code == 401
    data = response.json()
    assert data['success'] is False
    assert data['error']['code'] == 'CREDENCIALES_INCORRECTAS'


@pytest.mark.django_db
def test_login_correo_no_existe(client: Client):
    response = client.post(
        URL_LOGIN,
        {'correo': 'noexiste@tecsup.edu.pe', 'password': 'cualquier'},
        content_type='application/json',
    )
    assert response.status_code == 401
    data = response.json()
    assert data['success'] is False
    assert data['error']['code'] == 'CREDENCIALES_INCORRECTAS'


@pytest.mark.django_db
def test_login_usuario_inactivo(client: Client, db):
    usuario = Usuario.objects.create(
        nombre='Admin Inactivo',
        correo='inactivo@tecsup.edu.pe',
        password=make_password('password123'),
        rol=Usuario.Rol.ADMIN,
        activo=False,
    )
    Administrador.objects.create(usuario=usuario)

    response = client.post(
        URL_LOGIN,
        {'correo': 'inactivo@tecsup.edu.pe', 'password': 'password123'},
        content_type='application/json',
    )
    assert response.status_code == 401
    data = response.json()
    assert data['success'] is False
    assert data['error']['code'] == 'CREDENCIALES_INCORRECTAS'
