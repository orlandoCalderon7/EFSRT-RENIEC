// Archivo principal de JavaScript para la aplicación de gestión de citas y trámites en RENIEC Sechura
const API_BASE = window.location.origin;

const state = {
  ciudadano: null,
  ciudadanoPrevalidado: null,
  dniPrevalidado: null,
  imagenBiofacial: null,
  cameraStream: null,
  tramite: null,
  horarios: [],
  fechasDisponibles: [],
  fechaSeleccionada: null,
  horarioSeleccionado: null,
  citaRegistrada: false,
  reprogramandoCita: null,
  citasCiudadano: []
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

const toast = $('#toast');
const modalOverlay = $('#modalOverlay');
const modalIcon = $('#modalIcon');
const modalTitle = $('#modalTitle');
const modalBody = $('#modalBody');
const modalActions = $('#modalActions');

const viewLogin = $('#viewLogin');
const viewBiofacial = $('#viewBiofacial');
const viewDashboard = $('#viewDashboard');
const viewTramite = $('#viewTramite');
const viewFechaCita = $('#viewFechaCita');
const viewHorarios = $('#viewHorarios');
const viewMisTramites = $('#viewMisTramites');

const formLogin = $('#formLogin');
const formTramite = $('#formTramite');
const ciudadanoBox = $('#ciudadanoBox');
const dashboardNombre = $('#dashboardNombre');
const biofacialCiudadanoBox = $('#biofacialCiudadanoBox');
const cameraBox = $('#cameraBox');
const cameraVideo = $('#cameraVideo');
const cameraCanvas = $('#cameraCanvas');
const fotoPreview = $('#fotoPreview');
const biofacialStatus = $('#biofacialStatus');
const tramiteResumen = $('#tramiteResumen');
const fechaResumen = $('#fechaResumen');

const btnLogin = $('#btnLogin');
const btnMostrarTramite = $('#btnMostrarTramite');
const btnMisTramites = $('#btnMisTramites');
const btnCambiarCiudadano = $('#btnCambiarCiudadano');
const btnVolverDesdeBiofacial = $('#btnVolverDesdeBiofacial');
const btnActivarCamara = $('#btnActivarCamara');
const btnCapturarRostro = $('#btnCapturarRostro');
const btnValidarBiofacial = $('#btnValidarBiofacial');
const btnVolverDesdeTramite = $('#btnVolverDesdeTramite');
const btnVolverDesdeFecha = $('#btnVolverDesdeFecha');
const btnVolverDesdeHorarios = $('#btnVolverDesdeHorarios');
const btnVolverDesdeMisTramites = $('#btnVolverDesdeMisTramites');

const diasGrid = $('#diasGrid');
const tituloHorarios = $('#tituloHorarios');
const horariosGrid = $('#horariosGrid');
const misTramitesBox = $('#misTramitesBox');

const diasSemana = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
const diaIndex = {
  'Domingo': 0,
  'Lunes': 1,
  'Martes': 2,
  'Miércoles': 3,
  'Miercoles': 3,
  'Jueves': 4,
  'Viernes': 5,
  'Sábado': 6,
  'Sabado': 6
};

function showToast(message, isError = false) {
  toast.textContent = message;
  toast.classList.toggle('error', isError);
  toast.classList.remove('hidden');
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => toast.classList.add('hidden'), 3400);
}

function showModal({ title, body, icon = '✓', iconType = 'success', actions = [] }) {
  modalTitle.textContent = title;
  modalBody.innerHTML = body;
  modalIcon.textContent = icon;
  modalIcon.className = `modal-icon ${iconType === 'success' ? '' : iconType}`;
  modalActions.innerHTML = '';

  actions.forEach((action) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = action.text;
    button.className = `modal-btn ${action.variant === 'secondary' ? 'secondary' : ''}`;
    button.addEventListener('click', () => {
      if (action.close !== false) closeModal();
      if (typeof action.onClick === 'function') action.onClick();
    });
    modalActions.appendChild(button);
  });

  if (!actions.length) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'modal-btn';
    button.textContent = 'Aceptar';
    button.addEventListener('click', closeModal);
    modalActions.appendChild(button);
  }

  modalOverlay.classList.remove('hidden');
  modalOverlay.setAttribute('aria-hidden', 'false');
}

function closeModal() {
  modalOverlay.classList.add('hidden');
  modalOverlay.setAttribute('aria-hidden', 'true');
}

function showView(view) {
  $$('.view').forEach((item) => item.classList.add('hidden'));
  view.classList.remove('hidden');
  window.scrollTo({ top: 0, behavior: 'auto' });
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = typeof payload === 'string' ? payload : JSON.stringify(payload);
    throw new Error(message || 'Ocurrió un error en la solicitud.');
  }

  return payload;
}

function getNombreCiudadano(ciudadano) {
  const persona = ciudadano?.datosPersonales || {};
  return `${persona.nombres || ''} ${persona.apellidos || ''}`.replace(/\s+/g, ' ').trim() || 'Ciudadano validado';
}

function renderCiudadano(ciudadano) {
  const nombre = getNombreCiudadano(ciudadano);
  if (dashboardNombre) dashboardNombre.textContent = nombre;
  if (ciudadanoBox) ciudadanoBox.innerHTML = '';
}

function renderPrevalidacion(ciudadano) {
  if (!biofacialCiudadanoBox) return;
  biofacialCiudadanoBox.innerHTML = `<strong>${getNombreCiudadano(ciudadano)}</strong>`;
}

function mostrarModalInicioBiofacial(ciudadano) {
  const nombre = getNombreCiudadano(ciudadano);
  showModal({
    title: `Hola, ${nombre}`,
    icon: '✓',
    body: `
      <p>Para continuar necesitamos validar tu identidad mediante nuestro sistema de validación biofacial.</p>
      <p>Presiona <strong>Continuar</strong> para activar tu cámara y capturar tu rostro.</p>
    `,
    actions: [
      { text: 'Cambiar DNI', variant: 'secondary', onClick: resetSession },
      { text: 'Continuar', onClick: () => activarCamaraBiofacial() }
    ]
  });
}

function formatDate(fecha) {
  if (!fecha) return '-';
  const [year, month, day] = String(fecha).split('-');
  return year && month && day ? `${day}/${month}/${year}` : fecha;
}

function formatLongDate(fecha) {
  if (!fecha) return '-';
  const [year, month, day] = String(fecha).split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return `${diasSemana[date.getDay()]} ${String(day).padStart(2, '0')}/${String(month).padStart(2, '0')}/${year}`;
}

function formatHour(hora) {
  return hora ? String(hora).slice(0, 5) : '-';
}

function getCitaFechaTexto(cita) {
  if (cita?.fechaCita) return formatLongDate(cita.fechaCita);
  if (cita?.horario?.diaSemana) return `día ${cita.horario.diaSemana}`;
  return 'fecha pendiente de asignación';
}

function getCitaHoraTexto(cita) {
  return formatHour(cita?.horaCita || cita?.horario?.horaInicio);
}

function getTipoTramiteTexto(tramite) {
  return tramite?.tipoTramite?.nombre || $('#tipoTramite').selectedOptions?.[0]?.textContent || 'Trámite registrado';
}

function toIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getNextDateForHorario(horario) {
  const now = new Date();
  const todayIndex = now.getDay(); // 0=Dom, 1=Lun, ...
  const target = diaIndex[horario.diaSemana] ?? todayIndex;

  // Calcular días de diferencia
  let diff = target - todayIndex;
  if (diff < 0) diff += 7;

  // Construir la fecha objetivo sin problemas de zona horaria
  const date = new Date(now.getFullYear(), now.getMonth(), now.getDate() + diff);

  // Verificar si el horario de hoy ya pasó
  if (diff === 0 && horario.horaInicio) {
    const [hour = 0, minute = 0] = String(horario.horaInicio).split(':').map(Number);
    const startDateTime = new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour, minute);
    if (startDateTime <= now) {
      date.setDate(date.getDate() + 7);
    }
  }

  return toIsoDate(date);
}


function getFechasDisponibles(horarios) {
  const map = new Map();

  horarios.forEach((horario) => {
    const fecha = getNextDateForHorario(horario);
    // Clave única por fecha + idHorario para evitar duplicados
    const key = `${fecha}_${horario.idHorario}`;
    if (!map.has(fecha)) {
      map.set(fecha, {
        fecha,
        dia: horario.diaSemana || diasSemana[new Date(fecha + 'T00:00:00').getDay()],
        items: []
      });
    }
    // Solo agregar si este horario no está ya en esa fecha
    const grupo = map.get(fecha);
    const yaExiste = grupo.items.some((item) => item.idHorario === horario.idHorario);
    if (!yaExiste) {
      grupo.items.push(horario);
    }
  });

  return Array.from(map.values()).sort((a, b) => a.fecha.localeCompare(b.fecha));
}


function resetTramiteFlow() {
  state.tramite = null;
  state.horarios = [];
  state.fechasDisponibles = [];
  state.fechaSeleccionada = null;
  state.horarioSeleccionado = null;
  state.citaRegistrada = false;
  state.reprogramandoCita = null;
  diasGrid.innerHTML = '';
  horariosGrid.innerHTML = '';
  tramiteResumen.innerHTML = '';
  fechaResumen.innerHTML = '';
  $('#tipoTramite').value = '1';
}

function stopCamera() {
  if (state.cameraStream) {
    state.cameraStream.getTracks().forEach((track) => track.stop());
    state.cameraStream = null;
  }

  if (cameraVideo) {
    cameraVideo.srcObject = null;
  }
  const modalVideo = $('#modalCameraVideo');
  if (modalVideo) {
    modalVideo.srcObject = null;
  }
  if (cameraBox) {
    cameraBox.classList.add('hidden');
  }
}

function resetBiofacial() {
  stopCamera();
  state.ciudadanoPrevalidado = null;
  state.dniPrevalidado = null;
  state.imagenBiofacial = null;
  if (fotoPreview) {
    fotoPreview.src = '';
    fotoPreview.classList.add('hidden');
  }
  if (cameraBox) cameraBox.classList.add('hidden');
  if (cameraVideo) cameraVideo.classList.remove('hidden');
  if (biofacialStatus) {
    biofacialStatus.textContent = '';
    biofacialStatus.classList.remove('ready');
  }
  if (btnCapturarRostro) btnCapturarRostro.disabled = true;
  if (btnValidarBiofacial) btnValidarBiofacial.disabled = true;
}

function resetSession() {
  state.ciudadano = null;
  state.citasCiudadano = [];
  resetBiofacial();
  resetTramiteFlow();
  $('#dni').value = '';
  if (dashboardNombre) dashboardNombre.textContent = 'Ciudadano validado';
  showView(viewLogin);
  setTimeout(() => $('#dni').focus(), 50);
}

async function obtenerCitaActiva() {
  if (!state.ciudadano?.idUsuario) return null;
  const data = await request(`${API_BASE}/api/citas/ciudadano/${state.ciudadano.idUsuario}/activa`);
  return data?.tieneCitaActiva ? data.cita : null;
}

function renderCitaActivaModal(cita) {
  showModal({
    title: 'Ya tienes una cita pendiente',
    icon: '!',
    iconType: 'warning',
    body: `
      <p>Actualmente tienes una cita registrada para el <strong>${getCitaFechaTexto(cita)}</strong> a horas <strong>${getCitaHoraTexto(cita)}</strong>.</p>
      <p>Ticket: <strong>${cita.ticketDigital || '-'}</strong>.</p>
      <p>Desde tus trámites puedes revisar el detalle, reprogramar la cita o cancelar el trámite si corresponde.</p>
    `,
    actions: [
      { text: 'Cerrar', variant: 'secondary' },
      { text: 'Ver mis trámites', onClick: () => cargarMisTramites() }
    ]
  });
}

async function validarCitaActivaAntesDeTramite() {
  try {
    const citaActiva = await obtenerCitaActiva();
    if (citaActiva) {
      renderCitaActivaModal(citaActiva);
      return false;
    }
    return true;
  } catch (error) {
    showToast(error.message, true);
    return false;
  }
}

async function cargarFechasDisponibles() {
  diasGrid.innerHTML = '<div class="empty-state">Cargando fechas disponibles...</div>';
  horariosGrid.innerHTML = '';

  try {
    state.horarios = await request(`${API_BASE}/api/citas/horarios/1`);


    console.log('Horarios recibidos:', state.horarios.length, state.horarios);
    state.fechasDisponibles = getFechasDisponibles(state.horarios);
    console.log('Fechas agrupadas:', state.fechasDisponibles);
    state.fechasDisponibles.forEach(f => console.log(f.fecha, '→', f.items.length, 'items'));


    if (!state.horarios.length) {
      diasGrid.innerHTML = '<div class="empty-state">No hay fechas disponibles para reservar cita.</div>';
      return;
    }

    state.fechasDisponibles = getFechasDisponibles(state.horarios);
    diasGrid.innerHTML = state.fechasDisponibles.map(({ fecha, items }) => `
      <button type="button" class="day-card" data-fecha="${fecha}">
        <strong>${formatLongDate(fecha)}</strong>
        <span>${items.length} horario${items.length === 1 ? '' : 's'} disponible${items.length === 1 ? '' : 's'}</span>
      </button>
    `).join('');

    diasGrid.querySelectorAll('.day-card').forEach((button) => {
      button.addEventListener('click', () => seleccionarFecha(button.dataset.fecha));
    });
  } catch (error) {
    diasGrid.innerHTML = '<div class="empty-state">No se pudieron cargar las fechas disponibles.</div>';
    showToast(error.message, true);
  }
}

function seleccionarFecha(fecha) {
  if (state.citaRegistrada) return;

  const fechaData = state.fechasDisponibles.find((item) => item.fecha === fecha);
  if (!fechaData) return;

  state.fechaSeleccionada = fechaData;
  state.horarioSeleccionado = null;

  fechaResumen.innerHTML = `
    <strong>${formatLongDate(fechaData.fecha)}</strong>
    <span>Selecciona uno de los horarios disponibles para esta fecha.</span>
  `;

  tituloHorarios.textContent = `${state.reprogramandoCita ? 'Nuevos horarios' : 'Horarios disponibles'} para ${formatLongDate(fechaData.fecha)}`;
  horariosGrid.innerHTML = fechaData.items.map((horario) => `
    <div class="horario-card">
      <strong>${formatHour(horario.horaInicio)} - ${formatHour(horario.horaFin)}</strong>
      <span>Cupos usados: ${horario.citasActuales}/${horario.capacidadMax}</span>
      <button type="button" data-id-horario="${horario.idHorario}">Seleccionar horario</button>
    </div>
  `).join('');

  horariosGrid.querySelectorAll('[data-id-horario]').forEach((button) => {
    button.addEventListener('click', () => confirmarReserva(Number(button.dataset.idHorario)));
  });

  showView(viewHorarios);
}

function confirmarReserva(idHorario) {
  if (state.citaRegistrada) {
    showToast('La cita de este trámite ya fue registrada.', true);
    return;
  }

  const horario = state.horarios.find((item) => item.idHorario === idHorario);
  if (!horario || !state.tramite || !state.fechaSeleccionada) return;

  state.horarioSeleccionado = horario;
  const esReprogramacion = Boolean(state.reprogramandoCita);
  showModal({
    title: esReprogramacion ? 'Confirmar reprogramación' : 'Confirmar cita',
    icon: '!',
    iconType: 'warning',
    body: `
      <p>${esReprogramacion ? 'Vas a reprogramar tu cita para el' : 'Vas a reservar una cita para el'} <strong>${formatLongDate(state.fechaSeleccionada.fecha)}</strong>, de <strong>${formatHour(horario.horaInicio)}</strong> a <strong>${formatHour(horario.horaFin)}</strong>.</p>
      <p>¿Deseas continuar?</p>
    `,
    actions: [
      { text: 'Cancelar', variant: 'secondary' },
      { text: 'Confirmar', onClick: () => reservarCita(idHorario) }
    ]
  });
}

async function reservarCita(idHorario) {
  if (state.citaRegistrada) return;

  state.citaRegistrada = true;
  horariosGrid.querySelectorAll('button').forEach((button) => button.disabled = true);

  const esReprogramacion = Boolean(state.reprogramandoCita);

  try {
    const url = esReprogramacion
      ? `${API_BASE}/api/citas/${state.reprogramandoCita.idCita}/reprogramar?idHorario=${idHorario}&fechaCita=${state.fechaSeleccionada.fecha}`
      : `${API_BASE}/api/citas/reservar?idTramite=${state.tramite.idTramite}&idHorario=${idHorario}&fechaCita=${state.fechaSeleccionada.fecha}`;

    const cita = await request(url, { method: 'POST' });

    const horario = cita.horario || state.horarioSeleccionado || {};
    showToast(esReprogramacion ? 'Cita reprogramada correctamente.' : 'Cita registrada correctamente.');
    showModal({
      title: esReprogramacion ? 'Su cita ha sido reprogramada' : 'Su cita ha sido registrada',
      body: `
        <p>Su cita para el <strong>${formatLongDate(cita.fechaCita || state.fechaSeleccionada.fecha)}</strong> a horas <strong>${formatHour(cita.horaCita || horario.horaInicio)}</strong> ${esReprogramacion ? 'ha sido reprogramada correctamente' : 'ha sido registrada correctamente'}.</p>
        <p>El número de ticket es <strong>${cita.ticketDigital || '-'}</strong>.</p>
        <p>Sírvase presentarse puntualmente en el horario establecido.</p>
      `,
      actions: [
        {
          text: 'Ver mis trámites',
          onClick: () => {
            resetTramiteFlow();
            cargarMisTramites();
          }
        },
        {
          text: 'Inicio',
          variant: 'secondary',
          onClick: () => {
            resetTramiteFlow();
            showView(viewDashboard);
          }
        }
      ]
    });
  } catch (error) {
    state.citaRegistrada = false;
    horariosGrid.querySelectorAll('button').forEach((button) => button.disabled = false);
    showToast(error.message, true);
    showModal({
      title: esReprogramacion ? 'No se pudo reprogramar la cita' : 'No se pudo registrar la cita',
      icon: '×',
      iconType: 'error',
      body: `<p>${error.message}</p>`,
      actions: [{ text: 'Aceptar' }]
    });
  }
}


async function crearTramite() {
  const idTipo = Number($('#tipoTramite').value);

  try {
    const citaActiva = await obtenerCitaActiva();
    if (citaActiva) {
      renderCitaActivaModal(citaActiva);
      return;
    }
  } catch (error) {
    showToast(error.message, true);
    return;
  }

  const body = {
    ciudadano: { idUsuario: state.ciudadano.idUsuario },
    tipoTramite: { idTipo }
  };

  try {
    const tramite = await request(`${API_BASE}/api/tramites`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    state.tramite = tramite;
    state.citaRegistrada = false;
    tramiteResumen.innerHTML = `
      <strong>${getTipoTramiteTexto(tramite)}</strong>
      <span>Código de trámite: ${tramite.codigoTramite || '-'} · Fecha de solicitud: ${formatDate(tramite.fechaSolicitud)}</span>
    `;

    showToast('Trámite iniciado correctamente.');
    showModal({
      title: 'Trámite iniciado',
      body: `
        <p>Se ha iniciado el trámite con código <strong>${tramite.codigoTramite || '-'}</strong>.</p>
        <p>Fecha de solicitud: <strong>${formatDate(tramite.fechaSolicitud)}</strong>.</p>
        <p>Presione continuar para seleccionar la fecha disponible.</p>
      `,
      actions: [
        {
          text: 'Continuar',
          onClick: async () => {
            showView(viewFechaCita);
            await cargarFechasDisponibles();
          }
        }
      ]
    });
  } catch (error) {
    showToast(error.message, true);
    showModal({
      title: 'No se pudo iniciar el trámite',
      icon: '×',
      iconType: 'error',
      body: `<p>${error.message}</p>`,
      actions: [{ text: 'Aceptar' }]
    });
  }
}

function renderCameraModal() {
  showModal({
    title: 'Validación Facial',
    icon: '●',
    iconType: 'warning',
    body: `
      <p>Coloca tu rostro al centro de la cámara y presiona <strong>Capturar rostro</strong>.</p>
      <div id="modalCameraArea" class="camera-box modal-camera-box">
        <video id="modalCameraVideo" autoplay playsinline muted></video>
        <canvas id="modalCameraCanvas" class="hidden"></canvas>
      </div>
      <div id="modalBiofacialLoading" class="biofacial-loading hidden">
        <div class="loading-spinner" aria-hidden="true"></div>
        <strong>Validando identidad...</strong>
        <span>Comparando la captura facial con el DNI ingresado.</span>
      </div>
    `,
    actions: [
      { text: 'Cancelar', variant: 'secondary', onClick: () => stopCamera() },
      { text: 'Capturar rostro', close: false, onClick: () => capturarRostro() }
    ]
  });
}

async function activarCamaraBiofacial() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    showModal({
      title: 'Cámara no disponible',
      icon: '×',
      iconType: 'error',
      body: '<p>Tu navegador no permite acceder a la cámara. Prueba desde Chrome, Edge o un navegador compatible en localhost.</p>',
      actions: [{ text: 'Aceptar' }]
    });
    return;
  }

  try {
    stopCamera();
    state.imagenBiofacial = null;
    renderCameraModal();

    const video = $('#modalCameraVideo');
    const captureButton = Array.from(modalActions.querySelectorAll('button'))
      .find((button) => button.textContent === 'Capturar rostro');
    if (captureButton) captureButton.disabled = true;

    if (biofacialStatus) {
      biofacialStatus.textContent = '';
      biofacialStatus.classList.remove('ready');
    }

    state.cameraStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user', width: { ideal: 720 }, height: { ideal: 720 } },
      audio: false
    });

    if (!video) throw new Error('No se pudo preparar la vista de cámara.');
    video.srcObject = state.cameraStream;
    await video.play();

    if (captureButton) captureButton.disabled = false;
    if (biofacialStatus) {
      biofacialStatus.textContent = '';
      biofacialStatus.classList.remove('ready');
    }
  } catch (error) {
    stopCamera();
    showToast('No se pudo acceder a la cámara.', true);
    showModal({
      title: 'Permiso de cámara requerido',
      icon: '×',
      iconType: 'error',
      body: '<p>Autoriza el acceso a la cámara para continuar con la validación biofacial.</p>',
      actions: [{ text: 'Aceptar' }]
    });
  }
}

async function capturarRostro() {
  const video = $('#modalCameraVideo');
  const canvas = $('#modalCameraCanvas');

  if (!state.cameraStream || !video || !canvas || !video.videoWidth) {
    showToast('Primero activa la cámara.', true);
    return;
  }

  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  const context = canvas.getContext('2d');
  context.drawImage(video, 0, 0, canvas.width, canvas.height);

  state.imagenBiofacial = canvas.toDataURL('image/jpeg', 0.88);
  stopCamera();

  const cameraArea = $('#modalCameraArea');
  const loadingBox = $('#modalBiofacialLoading');
  if (cameraArea) cameraArea.classList.add('hidden');
  if (loadingBox) loadingBox.classList.remove('hidden');

  modalTitle.textContent = 'Validando identidad';
  modalIcon.textContent = '…';
  modalIcon.className = 'modal-icon loading';
  modalActions.innerHTML = '';

  if (biofacialStatus) {
    biofacialStatus.textContent = '';
    biofacialStatus.classList.add('ready');
  }

  showToast('Rostro capturado. Validando identidad...');
  await new Promise((resolve) => setTimeout(resolve, 1200));
  await validarBiofacial();
}

async function validarBiofacial() {
  if (!state.ciudadanoPrevalidado || !state.dniPrevalidado) {
    showToast('Primero valida tu DNI.', true);
    return;
  }

  if (!state.imagenBiofacial) {
    showToast('Primero captura tu rostro.', true);
    return;
  }

  try {
    const resultado = await request(`${API_BASE}/api/auth/biofacial/verificar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        dni: state.dniPrevalidado,
        imagenBase64: state.imagenBiofacial
      })
    });

    if (!resultado.aprobado) {
      showToast(resultado.mensaje || 'La validación biofacial no fue aprobada.', true);
      showModal({
        title: 'Validación no aprobada',
        icon: '×',
        iconType: 'error',
        body: `<p>${resultado.mensaje || 'El rostro capturado no corresponde al titular del DNI.'}</p>`,
        actions: [
          { text: 'Cambiar DNI', variant: 'secondary', onClick: resetSession },
          { text: 'Intentar nuevamente', onClick: () => activarCamaraBiofacial() }
        ]
      });
      return;
    }

    state.ciudadano = resultado.ciudadano || state.ciudadanoPrevalidado;
    renderCiudadano(state.ciudadano);
    stopCamera();
    closeModal();
    showView(viewDashboard);
    showToast('Identidad validada correctamente.');
  } catch (error) {
    showToast(error.message, true);
    showModal({
      title: 'Error en validación biofacial',
      icon: '×',
      iconType: 'error',
      body: `<p>${error.message}</p>`,
      actions: [
        { text: 'Cambiar DNI', variant: 'secondary', onClick: resetSession },
        { text: 'Intentar nuevamente', onClick: () => activarCamaraBiofacial() }
      ]
    });
  }
}


formLogin.addEventListener('submit', async (event) => {
  event.preventDefault();
  const dni = $('#dni').value.trim();

  if (!/^\d{8}$/.test(dni)) {
    showToast('Ingresa un DNI válido de 8 dígitos.', true);
    return;
  }

  btnLogin.disabled = true;

  try {
    resetBiofacial();
    const data = await request(`${API_BASE}/api/auth/prevalidar?dni=${encodeURIComponent(dni)}`, { method: 'POST' });
    const ciudadano = data.ciudadano;
    state.ciudadanoPrevalidado = ciudadano;
    state.dniPrevalidado = dni;
    resetTramiteFlow();
    renderPrevalidacion(ciudadano);
    mostrarModalInicioBiofacial(ciudadano);
  } catch (error) {
    showToast(error.message, true);
    showModal({
      title: 'No se pudo validar el ciudadano',
      icon: '×',
      iconType: 'error',
      body: `<p>${error.message}</p>`,
      actions: [{ text: 'Aceptar' }]
    });
  } finally {
    btnLogin.disabled = false;
  }
});

formTramite.addEventListener('submit', (event) => {
  event.preventDefault();

  if (!state.ciudadano) {
    showToast('Primero valida tu DNI.', true);
    return;
  }

  const tipo = $('#tipoTramite').selectedOptions[0].textContent;
  showModal({
    title: 'Confirmar trámite',
    icon: '!',
    iconType: 'warning',
    body: `<p>Se iniciará el trámite de <strong>${tipo}</strong>. ¿Deseas continuar?</p>`,
    actions: [
      { text: 'Cancelar', variant: 'secondary' },
      { text: 'Confirmar', onClick: crearTramite }
    ]
  });
});

btnMostrarTramite.addEventListener('click', async () => {
  if (!state.ciudadano) {
    showToast('Primero valida tu DNI.', true);
    return;
  }

  const puedeContinuar = await validarCitaActivaAntesDeTramite();
  if (!puedeContinuar) return;

  resetTramiteFlow();
  showView(viewTramite);
});

async function cargarMisTramites() {
  if (!state.ciudadano) {
    showToast('Primero valida tu DNI.', true);
    return;
  }

  showView(viewMisTramites);
  misTramitesBox.innerHTML = '<div class="empty-state">Cargando trámites...</div>';

  try {
    const [tramites, citas] = await Promise.all([
      request(`${API_BASE}/api/tramites/ciudadano/${state.ciudadano.idUsuario}`),
      request(`${API_BASE}/api/citas/ciudadano/${state.ciudadano.idUsuario}`)
    ]);

    state.citasCiudadano = citas;

    if (!tramites.length) {
      misTramitesBox.innerHTML = '<div class="empty-state">No tienes trámites registrados.</div>';
      return;
    }

    const tramitesOrdenados = [...tramites].sort((a, b) => {
      const fechaA = a.fechaSolicitud ? new Date(a.fechaSolicitud).getTime() : 0;
      const fechaB = b.fechaSolicitud ? new Date(b.fechaSolicitud).getTime() : 0;

      if (fechaA !== fechaB) {
        return fechaB - fechaA;
      }

      return (b.idTramite || 0) - (a.idTramite || 0);
    });

    misTramitesBox.innerHTML = tramitesOrdenados.map((tramite) => renderTramiteItem(tramite, citas.find((item) => item.tramite?.idTramite === tramite.idTramite))).join('');

    misTramitesBox.querySelectorAll('[data-action="reprogramar"]').forEach((button) => {
      button.addEventListener('click', () => iniciarReprogramacion(Number(button.dataset.idTramite), Number(button.dataset.idCita)));
    });

    misTramitesBox.querySelectorAll('[data-action="cancelar"]').forEach((button) => {
      button.addEventListener('click', () => confirmarCancelacionTramite(Number(button.dataset.idTramite)));
    });

    misTramitesBox.querySelectorAll('[data-action="programar"]').forEach((button) => {
      button.addEventListener('click', () => iniciarProgramacionTramiteExistente(Number(button.dataset.idTramite)));
    });

    showToast('Trámites cargados correctamente.');
  } catch (error) {
    misTramitesBox.innerHTML = '<div class="empty-state">No se pudieron cargar tus trámites.</div>';
    showToast(error.message, true);
  }
}

function renderTramiteItem(tramite, cita) {
  const tramiteCancelado = tramite.estado === 'CANCELADO';
  const citaCancelada = cita?.estadoCita === 'CANCELADA';
  const puedeCancelar = !['CANCELADO', 'ENTREGADO'].includes(tramite.estado);
  const puedeReprogramar = cita && !tramiteCancelado && !citaCancelada;
  const puedeProgramar = !cita && !tramiteCancelado;

  const citaHtml = cita ? `
    <div class="cita-line">
      Cita: ${getCitaFechaTexto(cita)} · ${getCitaHoraTexto(cita)} · Ticket ${cita.ticketDigital || '-'}
    </div>
    <div class="badge ${['FINALIZADA', 'CANCELADA'].includes(cita.estadoCita) ? 'muted' : ''}">${cita.estadoCita || 'Sin estado de cita'}</div>
  ` : '<div class="cita-line">Cita pendiente de programación.</div>';

  const acciones = [];
  if (puedeProgramar) {
    acciones.push(`<button type="button" class="small-btn" data-action="programar" data-id-tramite="${tramite.idTramite}">Programar cita</button>`);
  }
  if (puedeReprogramar) {
    acciones.push(`<button type="button" class="small-btn" data-action="reprogramar" data-id-tramite="${tramite.idTramite}" data-id-cita="${cita.idCita}">Reprogramar cita</button>`);
  }
  if (puedeCancelar) {
    acciones.push(`<button type="button" class="small-btn danger" data-action="cancelar" data-id-tramite="${tramite.idTramite}">Cancelar trámite</button>`);
  }

  return `
    <div class="list-item">
      <strong>${tramite.codigoTramite || 'Trámite sin código'}</strong>
      <span>${tramite.tipoTramite?.nombre || 'Tipo de trámite no disponible'}</span><br>
      <span>Fecha de solicitud: ${formatDate(tramite.fechaSolicitud)}</span>
      <div class="badge ${tramiteCancelado ? 'danger' : ''}">${tramite.estado || 'Sin estado'}</div>
      ${citaHtml}
      ${acciones.length ? `<div class="item-actions">${acciones.join('')}</div>` : ''}
    </div>
  `;
}

async function iniciarProgramacionTramiteExistente(idTramite) {
  const puedeContinuar = await validarCitaActivaAntesDeTramite();
  if (!puedeContinuar) return;

  state.tramite = { idTramite };
  state.reprogramandoCita = null;
  state.citaRegistrada = false;
  tramiteResumen.innerHTML = '<strong>Programación de cita</strong><span>Selecciona una fecha disponible para este trámite.</span>';
  showView(viewFechaCita);
  await cargarFechasDisponibles();
}

async function iniciarReprogramacion(idTramite, idCita) {
  const cita = state.citasCiudadano.find((item) => item.idCita === idCita);
  if (!cita) {
    showToast('No se encontró la cita seleccionada.', true);
    return;
  }

  showModal({
    title: 'Reprogramar cita',
    icon: '!',
    iconType: 'warning',
    body: `
      <p>Tu cita actual está registrada para el <strong>${getCitaFechaTexto(cita)}</strong> a horas <strong>${getCitaHoraTexto(cita)}</strong>.</p>
      <p>Seleccionarás una nueva fecha y horario disponible.</p>
    `,
    actions: [
      { text: 'Cancelar', variant: 'secondary' },
      {
        text: 'Continuar',
        onClick: async () => {
          state.tramite = { idTramite };
          state.reprogramandoCita = cita;
          state.citaRegistrada = false;
          tramiteResumen.innerHTML = '<strong>Reprogramación de cita</strong><span>Selecciona una nueva fecha disponible.</span>';
          showView(viewFechaCita);
          await cargarFechasDisponibles();
        }
      }
    ]
  });
}

function confirmarCancelacionTramite(idTramite) {
  showModal({
    title: 'Cancelar trámite',
    icon: '!',
    iconType: 'warning',
    body: '<p>El trámite quedará cancelado, pero seguirá visible en tu historial. Si tenía una cita activa, también será cancelada.</p><p>¿Deseas continuar?</p>',
    actions: [
      { text: 'Volver', variant: 'secondary' },
      { text: 'Cancelar trámite', onClick: () => cancelarTramite(idTramite) }
    ]
  });
}

async function cancelarTramite(idTramite) {
  try {
    await request(`${API_BASE}/api/tramites/${idTramite}/cancelar`, { method: 'PUT' });
    showToast('Trámite cancelado correctamente.');
    showModal({
      title: 'Trámite cancelado',
      body: '<p>El trámite fue cancelado correctamente y seguirá apareciendo en tu historial.</p>',
      actions: [{ text: 'Ver mis trámites', onClick: () => cargarMisTramites() }]
    });
  } catch (error) {
    showToast(error.message, true);
    showModal({
      title: 'No se pudo cancelar el trámite',
      icon: '×',
      iconType: 'error',
      body: `<p>${error.message}</p>`,
      actions: [{ text: 'Aceptar' }]
    });
  }
}

btnActivarCamara.addEventListener('click', activarCamaraBiofacial);
if (btnCapturarRostro) btnCapturarRostro.addEventListener('click', capturarRostro);
if (btnValidarBiofacial) btnValidarBiofacial.addEventListener('click', validarBiofacial);
btnVolverDesdeBiofacial.addEventListener('click', () => {
  showModal({
    title: 'Cambiar DNI',
    icon: '!',
    iconType: 'warning',
    body: '<p>Se cancelará la validación biofacial actual para ingresar otro DNI.</p>',
    actions: [
      { text: 'Cancelar', variant: 'secondary' },
      { text: 'Continuar', onClick: resetSession }
    ]
  });
});

btnMisTramites.addEventListener('click', cargarMisTramites);

btnCambiarCiudadano.addEventListener('click', () => {
  showModal({
    title: 'Salir',
    icon: '!',
    iconType: 'warning',
    body: '<p>Se cerrará tu sesión actual y volverás a la pantalla de ingreso.</p>',
    actions: [
      { text: 'No, volver', variant: 'secondary' },
      { text: 'Salir', onClick: resetSession }
    ]
  });
});

btnVolverDesdeTramite.addEventListener('click', () => showView(viewDashboard));
btnVolverDesdeFecha.addEventListener('click', () => showView(viewTramite));
btnVolverDesdeHorarios.addEventListener('click', () => showView(viewFechaCita));
btnVolverDesdeMisTramites.addEventListener('click', () => showView(viewDashboard));

modalOverlay.addEventListener('click', (event) => {
  if (event.target === modalOverlay) closeModal();
});

document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape' && !modalOverlay.classList.contains('hidden')) closeModal();
});

document.addEventListener('DOMContentLoaded', () => {
  localStorage.removeItem('reniecCiudadano');
  sessionStorage.removeItem('reniecCiudadano');
  resetSession();
});
