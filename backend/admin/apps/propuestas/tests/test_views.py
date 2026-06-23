import pytest
from django.contrib.auth.hashers import make_password
from django.test import Client
from django.utils import timezone

from apps.proyectos.models import Administrador, Grupo, Proyecto, Propuesta, Usuario

URL_PROPUESTAS = '/api/admin/propuestas/'


@pytest.fixture
def admin_token(client: Client, db):
    usuario = Usuario.objects.create(
        nombre='Admin Test',
        correo='admin_propuestas@tecsup.edu.pe',
        password=make_password('passwordseguro'),
        rol=Usuario.Rol.ADMIN,
        activo=True,
    )
    Administrador.objects.create(usuario=usuario)
    response = client.post(
        '/api/admin/auth/login',
        {'correo': 'admin_propuestas@tecsup.edu.pe', 'password': 'passwordseguro'},
        content_type='application/json',
    )
    return response.json()['data']['access_token']


@pytest.fixture
def propuesta_pendiente(db):
    grupo = Grupo.objects.create(codigo_grupo='G-TEST-01', periodo='2026-I')
    return Propuesta.objects.create(
        grupo=grupo,
        nombre='Proyecto Test',
        descripcion='Descripción del proyecto test para revisión.',
        estado=Propuesta.Estado.PENDIENTE,
        fecha_envio=timezone.now(),
    )


@pytest.mark.django_db
def test_listar_propuestas_retorna_lista(client: Client, admin_token, propuesta_pendiente):
    response = client.get(
        URL_PROPUESTAS,
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 200
    data = response.json()
    assert data['success'] is True
    assert isinstance(data['data'], list)
    assert len(data['data']) >= 1


@pytest.mark.django_db
def test_listar_propuestas_requiere_autenticacion(client: Client, db):
    response = client.get(URL_PROPUESTAS)
    assert response.status_code == 401


@pytest.mark.django_db
def test_aprobar_propuesta_exitoso(client: Client, admin_token, propuesta_pendiente):
    pk = propuesta_pendiente.id
    response = client.put(
        f'{URL_PROPUESTAS}{pk}/aprobar',
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 200
    data = response.json()
    assert data['success'] is True
    assert data['data']['estado'] == 'APROBADO'
    assert 'proyecto_id' in data['data']

    propuesta_pendiente.refresh_from_db()
    assert propuesta_pendiente.estado == 'APROBADO'
    assert Proyecto.objects.filter(
        grupo=propuesta_pendiente.grupo, origen=Proyecto.Origen.NUEVO
    ).exists()


@pytest.mark.django_db
def test_aprobar_propuesta_crea_proyecto_en_desarrollo(client: Client, admin_token, propuesta_pendiente):
    pk = propuesta_pendiente.id
    client.put(
        f'{URL_PROPUESTAS}{pk}/aprobar',
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    proyecto = Proyecto.objects.get(
        grupo=propuesta_pendiente.grupo, origen=Proyecto.Origen.NUEVO
    )
    assert proyecto.estado == Proyecto.Estado.EN_DESARROLLO
    assert proyecto.nombre == propuesta_pendiente.nombre
    assert proyecto.ciclo == propuesta_pendiente.grupo.periodo
    assert proyecto.url is None


@pytest.mark.django_db
def test_aprobar_propuesta_no_encontrada(client: Client, admin_token, db):
    response = client.put(
        f'{URL_PROPUESTAS}999/aprobar',
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 404
    assert response.json()['error']['code'] == 'NO_ENCONTRADO'


@pytest.mark.django_db
def test_observar_propuesta_exitoso(client: Client, admin_token, propuesta_pendiente):
    pk = propuesta_pendiente.id
    response = client.put(
        f'{URL_PROPUESTAS}{pk}/observar',
        {'comentario': 'Definir mejor el alcance del MVP.'},
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 200
    data = response.json()
    assert data['success'] is True
    assert data['data']['estado'] == 'OBSERVADO'

    propuesta_pendiente.refresh_from_db()
    assert propuesta_pendiente.estado == 'OBSERVADO'
    assert propuesta_pendiente.comentario_observacion == 'Definir mejor el alcance del MVP.'


@pytest.mark.django_db
def test_observar_propuesta_sin_comentario(client: Client, admin_token, propuesta_pendiente):
    pk = propuesta_pendiente.id
    response = client.put(
        f'{URL_PROPUESTAS}{pk}/observar',
        {'comentario': ''},
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 400
    assert response.json()['error']['code'] == 'COMENTARIO_REQUERIDO'


@pytest.mark.django_db
def test_observar_propuesta_no_encontrada(client: Client, admin_token, db):
    response = client.put(
        f'{URL_PROPUESTAS}999/observar',
        {'comentario': 'Observación.'},
        content_type='application/json',
        HTTP_AUTHORIZATION=f'Bearer {admin_token}',
    )
    assert response.status_code == 404
    assert response.json()['error']['code'] == 'NO_ENCONTRADO'
