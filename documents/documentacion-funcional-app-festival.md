# Documentacion funcional de la app del festival

## 1. Vision general

DrinkCard MOA es una aplicacion para digitalizar la tarjeta de consumiciones de un festival. Sustituye la tarjeta fisica en papel por una cuenta digital con creditos, pagos online y tickets QR de un solo uso para canjear bebidas en barra.

La aplicacion se divide en tres experiencias principales:

- Experiencia de voluntario: registro, inicio de sesion, consulta de saldo, compra de creditos y generacion de QR para pedir bebidas.
- Experiencia de barra: lectura del QR, validacion del ticket y consumo de un credito.
- Experiencia de organizador: panel administrativo para consultar usuarios, cuentas de bebida, pagos y metricas operativas.

El backend esta desarrollado con Spring Boot y expone una API REST autenticada con JWT. El frontend esta desarrollado con React, TypeScript y Vite.

## 2. Usuarios y roles

### Voluntario

Es el usuario principal de la app movil. Puede crear una cuenta, iniciar sesion, consultar su tarjeta digital, comprar creditos y generar un QR para canjear una bebida.

Cuando un voluntario se registra, el sistema crea tambien su cuenta de DrinkCard asociada. Esa cuenta empieza con 0 creditos y estado `ACTIVE`.

### Personal de barra

Usa la pantalla de escaneo para leer tickets QR y aprobar consumiciones. En el estado actual de la aplicacion, la pantalla de escaneo esta disponible para usuarios autenticados. El modelo contempla el rol `BAR_STAFF`, pero la restriccion especifica por rol aun no esta aplicada en las rutas ni en el endpoint de consumo.

### Administrador

Tiene acceso al panel de organizador en `/admin`. Puede consultar usuarios registrados, cuentas DrinkCard, saldos y pagos. Los endpoints administrativos estan protegidos con rol `ADMIN`.

El proyecto incluye un usuario administrador inicial:

- Email: `admin@drinkcard.local`
- Rol: `ADMIN`

## 3. Acceso, registro y sesion

### Registro de usuario

Ruta frontend: `/register`

El formulario solicita:

- Nombre
- Apellido
- Email
- Contrasena

Reglas principales:

- El email debe tener formato valido.
- La contrasena debe tener minimo 8 caracteres.
- El email no puede estar repetido.
- El usuario se crea con rol `VOLUNTEER`.
- El usuario se crea en estado `ACTIVE`.
- Al registrarse, se publica un evento interno `UserRegisteredEvent`.
- El modulo DrinkCard escucha ese evento y crea una cuenta de bebida asociada al usuario.

Endpoint:

```http
POST /api/v1/auth/register
```

### Inicio de sesion

Ruta frontend: `/login`

El usuario inicia sesion con email y contrasena. Si las credenciales son correctas y el usuario esta activo, el backend devuelve una sesion con:

- Token JWT
- ID del usuario/voluntario
- Email
- Rol

La sesion se guarda en `localStorage` bajo la clave `drinkcard.session`, lo que permite mantener la sesion despues de recargar la pagina.

Endpoint:

```http
POST /api/v1/auth/login
```

### Cierre de sesion

El boton de logout borra la sesion local y obliga al usuario a volver a iniciar sesion para acceder a rutas protegidas.

### Rutas protegidas

Las rutas `/app`, `/bar/scanner` y `/admin` requieren sesion autenticada. Si no hay sesion, el frontend redirige a `/login`.

La ruta `/admin` exige rol `ADMIN`; si un usuario sin ese rol intenta entrar, se redirige a `/app`.

## 4. Experiencia del voluntario

### Pantalla principal de tarjeta

Ruta frontend: `/app`

Esta pantalla muestra el estado actual de la tarjeta digital del voluntario:

- Creditos disponibles.
- Nombre del voluntario.
- Estado de la cuenta DrinkCard.
- Ultima compra confirmada.
- Acciones principales: comprar creditos y generar un QR para pedir una bebida.

La pantalla consulta dos datos del backend:

```http
GET /api/v1/drink-card-accounts/me
GET /api/v1/users/me
```

La cuenta DrinkCard devuelve:

- `volunteerId`
- `credits`
- `status`
- `lastPurchaseTimestamp`

### Compra de creditos

Desde la pantalla principal, el voluntario puede comprar una tarjeta de 5 consumiciones.

Reglas de negocio:

- Cada compra anade 5 creditos.
- El precio de la tarjeta es 10 EUR.
- La compra se gestiona mediante SumUp.
- El sistema genera una clave de idempotencia por checkout para evitar duplicados.
- Si existe un pago con la misma clave de idempotencia, se devuelve el pago existente.
- La cuenta debe existir.
- La cuenta debe estar activa.
- No se permite mas de una compra el mismo dia para la misma cuenta.

Flujo:

1. El usuario pulsa `Buy 5 drinks`.
2. El frontend solicita al backend crear un checkout de pago.
3. El backend crea un pago `PENDING`.
4. El backend solicita a SumUp un hosted checkout.
5. El frontend muestra un enlace para abrir el checkout de SumUp.
6. Al volver a la app por la URL de exito, se confirma el pago.
7. Si SumUp indica pago realizado, el pago pasa a `SUCCESS` y se suman 5 creditos a la cuenta.
8. Si SumUp indica fallo o expiracion, el pago pasa a `FAILED` o `EXPIRED`.

Endpoints:

```http
POST /api/v1/payments/checkout
POST /api/v1/payments/{paymentId}/confirm
```

Estados de pago:

- `PENDING`: pago creado, pendiente de confirmacion.
- `SUCCESS`: pago confirmado y creditos anadidos.
- `FAILED`: pago fallido.
- `EXPIRED`: pago caducado.

### Retorno de pago

Ruta frontend: `/payment/success`

Esta pantalla se abre cuando el usuario vuelve desde el proveedor de pago. La app recupera el pago pendiente guardado en el navegador, llama al backend para confirmar el estado real del pago y muestra el resultado:

- Pago confirmado: muestra los creditos actuales.
- Pago pendiente, fallido o expirado: informa de que los creditos no se han anadido.
- Sin pago pendiente en el navegador: muestra advertencia.

## 5. Seleccion de bebida y QR

### Seleccion de bebida

Ruta frontend: `/app/drinks`

El voluntario puede elegir una bebida para generar un ticket QR. Las opciones visibles en el frontend son:

- Beer
- Wine
- Water
- Soft drink

Cada bebida cuesta 1 credito.

Al seleccionar una bebida, el frontend crea un drink ticket en el backend.

Endpoint:

```http
POST /api/v1/drink-tickets
```

Reglas para crear un ticket:

- El usuario debe tener una cuenta DrinkCard.
- La cuenta debe estar activa.
- La cuenta debe tener al menos 1 credito.
- No puede existir otro ticket activo pendiente para el mismo voluntario.
- El ticket se crea en estado `PENDING`.
- El ticket caduca a los 90 segundos.

El QR no contiene informacion sensible como saldo, email o tipo de usuario. Solo contiene el identificador del ticket:

```json
{
  "ticketId": "UUID_DEL_TICKET"
}
```

### Pantalla QR

Ruta frontend: `/app/qr`

La pantalla muestra:

- Nombre de la bebida seleccionada.
- Codigo QR.
- Cuenta atras hasta la expiracion.
- Indicador de ticket de un solo uso.
- ID del ticket.
- Estado actual del ticket.

La pantalla consulta el estado del ticket cada 3 segundos:

```http
GET /api/v1/drink-tickets/{ticketId}/status
```

Estados del ticket:

- `PENDING`: ticket valido, pendiente de escaneo.
- `CONSUMED`: ticket ya usado.
- `EXPIRED`: ticket caducado.

Si el ticket se consume o caduca, el QR se muestra como inactivo.

### Caducidad automatica de tickets

El backend incluye una tarea programada que busca tickets `PENDING` caducados y los marca como `EXPIRED`.

Configuracion actual:

```yaml
drinkcard:
  ticket-expiration-cleanup-delay-ms: 60000
```

Esto significa que el proceso de limpieza se ejecuta cada 60 segundos por defecto.

## 6. Experiencia de barra

### Escaner QR

Ruta frontend: `/bar/scanner`

La pantalla de barra permite consumir tickets de dos formas:

- Escaneo con camara usando `html5-qrcode`.
- Consumo manual introduciendo el ID del ticket o pegando el payload del QR.

Flujo:

1. La pantalla intenta activar la camara trasera del dispositivo.
2. Si no puede usar la camara trasera, busca una camara disponible.
3. Si no hay camara o el navegador bloquea permisos, se puede introducir el ticket manualmente.
4. Al leer el QR, extrae el `ticketId`.
5. Llama al backend para consumir el ticket.
6. Si el ticket es valido, muestra la bebida aprobada y el saldo restante.
7. Si el ticket no existe, esta caducado, ya fue consumido o no es valido, muestra rechazo.

Endpoint:

```http
POST /api/v1/drink-tickets/{ticketId}/consume
```

Reglas de consumo:

- El ticket debe existir.
- El ticket debe estar en estado `PENDING`.
- El ticket no debe estar caducado.
- La cuenta DrinkCard asociada debe existir.
- La cuenta debe tener creditos suficientes.
- Al consumir, el ticket pasa a `CONSUMED`.
- Al consumir, se descuenta 1 credito de la cuenta.
- Se guarda el identificador del usuario que realizo el consumo como `consumedByStaffId`.

Respuesta esperada:

- ID del ticket.
- Estado final.
- Tipo de bebida.
- Creditos restantes.

## 7. Panel del organizador

Ruta base frontend: `/admin`

El panel de organizador usa un layout de escritorio con navegacion lateral. Incluye:

- Dashboard.
- Volunteers.
- Shifts.
- Analytics.

Solo usuarios con rol `ADMIN` pueden acceder desde el frontend y desde los endpoints administrativos.

### Dashboard

Ruta frontend: `/admin`

El dashboard resume la actividad principal:

- Tarjetas activas.
- Creditos disponibles en todas las cuentas.
- Usuarios voluntarios.
- Pagos confirmados.
- Ingresos confirmados.
- Actividad reciente de pagos.
- Mezcla de bebidas mostrada como grafico simple.

Datos reales que carga del backend:

```http
GET /api/v1/admin/users
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/payments
```

Nota de estado actual:

- Las metricas de usuarios, cuentas y pagos vienen del backend.
- El bloque `Drink mix` usa datos estaticos en frontend; todavia no esta conectado a un endpoint real de analitica.

### Gestion y consulta de voluntarios

Ruta frontend: `/admin/volunteers`

Permite al administrador consultar voluntarios y sus cuentas DrinkCard.

Funciones:

- Listar usuarios con rol `VOLUNTEER`.
- Listar cuentas DrinkCard.
- Cruzar usuario y cuenta por ID de voluntario.
- Buscar por nombre, email, estado, ID o estado de cuenta.
- Refrescar datos.
- Seleccionar un voluntario para ver detalle.

Detalle mostrado:

- Nombre.
- Email.
- Estado del usuario.
- Estado de la cuenta.
- Creditos disponibles.
- Ultima compra.

Endpoints:

```http
GET /api/v1/admin/users?role=VOLUNTEER
GET /api/v1/admin/users/{userId}
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/drink-card-accounts/{volunteerId}
```

El listado de usuarios soporta filtros y paginacion:

- `role`
- `status`
- `email`
- `page`
- `size`
- `sort`

### Turnos de barra

Ruta frontend: `/admin/shifts`

La pantalla muestra una planificacion basica de turnos:

- Horario.
- Barra asignada.
- Numero de voluntarios.
- Estado del turno.

Estado actual:

- Los turnos estan definidos como datos estaticos en el frontend.
- El boton `New shift` existe visualmente, pero aun no crea turnos.
- No hay endpoints backend conectados para gestion real de turnos.

### Analiticas

Ruta frontend: `/admin/analytics`

La pantalla muestra una grafica de consumiciones por hora y notas operativas.

Estado actual:

- La grafica usa datos estaticos.
- El propio frontend indica que el endpoint en tiempo real esta pendiente.
- Las analiticas reales deberian construirse a partir de tickets consumidos, pagos y futuros eventos de transaccion.

## 8. Funcionalidades administrativas de backend

### Usuarios

Endpoints:

```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{userId}
```

Funciones:

- Listar usuarios.
- Filtrar por rol, estado o email.
- Paginacion.
- Ordenacion.
- Consultar detalle de un usuario.

Proteccion:

- Requiere JWT.
- Requiere rol `ADMIN`.

### Cuentas DrinkCard

Endpoints:

```http
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/drink-card-accounts/{volunteerId}
```

Funciones:

- Listar cuentas de bebida.
- Consultar cuenta por ID de voluntario.
- Ver creditos, estado y ultima compra.

Proteccion:

- Requiere JWT.
- Requiere rol `ADMIN`.

### Pagos

Endpoint:

```http
GET /api/v1/admin/payments
```

Filtros soportados:

- `volunteerId`
- `status`
- `from`
- `to`
- `page`
- `size`
- `sort`

Funciones:

- Revisar pagos de los voluntarios.
- Filtrar por voluntario.
- Filtrar por estado.
- Filtrar por rango temporal.
- Ordenar y paginar resultados.

Proteccion:

- Requiere JWT.
- Requiere rol `ADMIN`.

## 9. Reglas de negocio principales

### Creditos

- Cada tarjeta comprada suma 5 creditos.
- Cada bebida consumida descuenta 1 credito.
- No se puede generar un ticket si la cuenta no tiene creditos.
- El saldo vive en la cuenta DrinkCard, no en el QR.

### Compra

- El precio de una tarjeta es 10 EUR.
- Solo se permite una compra por dia para una cuenta.
- Solo una cuenta activa puede comprar.
- La confirmacion real depende del estado devuelto por SumUp.

### Ticket QR

- El ticket caduca a los 90 segundos.
- El QR es de un solo uso.
- Solo puede existir un ticket activo pendiente por voluntario.
- El ticket puede ser consumido, caducado o quedar pendiente.
- El QR solo contiene el ID del ticket.

### Seguridad

- La autenticacion se basa en JWT.
- Registro e inicio de sesion son publicos.
- El resto de endpoints requieren autenticacion.
- Los endpoints administrativos requieren rol `ADMIN`.
- La app es stateless en backend: no usa sesion de servidor.

### Estados

Usuarios:

- `ACTIVE`
- `SUSPENDED`
- `DELETED`

Cuentas DrinkCard:

- `ACTIVE`
- Otros estados del dominio pueden contemplarse para suspension, aunque la interfaz actual solo consulta el estado.

Pagos:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `EXPIRED`

Tickets:

- `PENDING`
- `CONSUMED`
- `EXPIRED`

## 10. Integraciones externas

### SumUp

La app usa SumUp para crear checkouts de pago y consultar el estado del checkout.

Configuracion principal:

- `SUMUP_BASE_URL`
- `SUMUP_API_KEY`
- `SUMUP_MERCHANT_CODE`
- `PAYMENT_FRONTEND_SUCCESS_URL`

El backend crea un hosted checkout con:

- Referencia del pago.
- Importe.
- Moneda `EUR`.
- Descripcion `Drink card - 5 credits`.
- URL de retorno al frontend.

### Base de datos

La base de datos usa migraciones Flyway.

Tablas principales:

- `users`: usuarios, credenciales, roles y estado.
- `drink_card_accounts`: saldo y estado de la tarjeta digital.
- `payments`: pagos y datos del proveedor.
- `drink_tickets`: tickets QR, estado, expiracion y consumo.

## 11. Limitaciones y pendientes funcionales

Estas partes existen visualmente o estan previstas, pero no estan completas como funcionalidad real:

- Gestion real de turnos de barra: la pantalla existe, pero usa datos estaticos y el boton de crear turno no persiste informacion.
- Analiticas reales: la grafica de consumiciones por hora usa datos estaticos.
- Mezcla real de bebidas: el dashboard muestra porcentajes fijos, no calculados desde tickets consumidos.
- Historial personal de pagos del voluntario: aun no hay pantalla de historial para el usuario.
- Historial personal de tickets del voluntario: aun no hay pantalla de historial.
- Ledger de transacciones de creditos: el saldo existe, pero no hay tabla historica detallada de movimientos de creditos.
- Restriccion estricta para personal de barra: el escaner funciona con usuarios autenticados, pero no exige todavia rol `BAR_STAFF`.
- Acciones admin de suspender/reactivar usuarios o cuentas: el modelo tiene estados, pero las acciones PATCH no estan implementadas en la API actual.
- Endpoints admin de detalle/listado de drink tickets: estan documentados como posibles historias futuras, pero no estan expuestos en la implementacion actual.

## 12. Resumen de flujos principales

### Flujo de alta

1. El usuario se registra.
2. El backend crea el usuario `VOLUNTEER`.
3. Se publica un evento de usuario registrado.
4. Se crea una cuenta DrinkCard asociada.
5. El usuario puede iniciar sesion.

### Flujo de compra

1. El voluntario entra en `/app`.
2. Pulsa comprar 5 bebidas.
3. El backend crea un pago pendiente.
4. SumUp crea el checkout.
5. El voluntario paga en SumUp.
6. La app confirma el pago al volver.
7. Si el pago es correcto, se suman 5 creditos.

### Flujo de consumicion

1. El voluntario elige una bebida.
2. El backend crea un ticket temporal.
3. La app muestra un QR con el ID del ticket.
4. Barra escanea el QR.
5. El backend valida el ticket.
6. Si es valido, marca el ticket como consumido.
7. El backend descuenta 1 credito.
8. Barra ve la aprobacion y el saldo restante.

### Flujo de administracion

1. El administrador inicia sesion.
2. Accede a `/admin`.
3. Consulta metricas generales.
4. Revisa voluntarios y cuentas.
5. Consulta pagos recientes y filtrables.
6. Usa pantallas de turnos y analiticas como base visual para futuras funcionalidades conectadas a backend.
