"use strict";

const byId = (id) => document.getElementById(id);
const statuses = {
  NORMAL: { label: "Normal", key: "normal", rank: 0, level: "34%" },
  ATENCAO: { label: "Atenção", key: "atencao", rank: 1, level: "67%" },
  VERMELHO: { label: "Alerta vermelho", key: "vermelho", rank: 2, level: "100%" },
};
const alertLevels = {
  VERDE: { label: "Normalizado", status: "NORMAL" },
  AMARELO: { label: "Alerta amarelo", status: "ATENCAO" },
  VERMELHO: { label: "Alerta vermelho", status: "VERMELHO" },
};
const dateFormat = new Intl.DateTimeFormat("pt-BR", {
  timeZone: "America/Sao_Paulo",
  day: "2-digit",
  month: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23",
});
const numberFormat = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 2 });
const listFormat = new Intl.ListFormat("pt-BR");
const maxPhotoLength = 260000;

let statusData = { pontos: [], alertas: [], relatos: [] };
let statusSignature;
let statusRequest = 0;
let teamSession = { authenticated: false, csrfToken: null };
let shareMessage = "";
let photo = null;
let photoRequest = 0;
let photoLoading = false;
let reportSubmitting = false;

function node(tag, attributes, ...children) {
  const element = document.createElement(tag);
  for (const [name, value] of Object.entries(attributes ?? {})) {
    if (value != null) element.setAttribute(name, value);
  }
  element.append(...children.filter((child) => child != null));
  return element;
}

function when(value) {
  if (!value) return "sem data";
  const timestamp = /^\d{4}-\d{2}-\d{2}T[\d:.]+$/.test(value) ? value + "-03:00" : value;
  const date = new Date(timestamp);
  return Number.isNaN(date.getTime()) ? "sem data" : dateFormat.format(date).replace(", ", " ");
}

function note(id, message, type = "") {
  const element = byId(id);
  element.className = type ? "msg " + type : "msg";
  element.textContent = message;
}

function whatsapp(message) {
  return "https://wa.me/?text=" + encodeURIComponent(message);
}

async function api(path, options) {
  const response = await fetch(path, options);
  if (response.status === 204) return;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = {
      400: "Confira os dados e tente novamente.",
      401: "Acesso não autorizado.",
      403: "A sessão precisa ser atualizada. Tente novamente.",
    }[response.status];
    const error = new Error(data.message || message || "Falha ao comunicar com o servidor.");
    error.status = response.status;
    throw error;
  }
  return data;
}

function jsonRequest(method, body, csrfToken) {
  const headers = { "Content-Type": "application/json" };
  if (csrfToken) headers["X-CSRF-TOKEN"] = csrfToken;
  return { method, headers, body: JSON.stringify(body) };
}

function renderBanner() {
  const { pontos, alertas } = statusData;
  const banner = byId("banner");
  const title = byId("banner-title");
  const detail = byId("banner-detail");
  const actions = byId("banner-actions");

  if (!pontos.length) {
    banner.dataset.s = "indisponivel";
    title.textContent = "Situação dos pontos indisponível";
    detail.textContent = "Consulte os comunicados da Defesa Civil de Itajubá.";
    actions.hidden = true;
    return;
  }

  const worst = pontos.reduce((current, point) =>
    statuses[point.status].rank > statuses[current].rank ? point.status : current, "NORMAL");
  const hot = pontos.filter((point) => point.status === worst);
  const names = listFormat.format(hot.map((point) => point.bairro));
  const latestAlert = alertas.find((alert) =>
    alertLevels[alert.nivel].status === worst && hot.some((point) => point.bairro === alert.bairro));
  const latestUpdate = pontos.map((point) => point.ts).filter(Boolean).sort().at(-1);

  banner.dataset.s = statuses[worst].key;
  title.textContent = worst === "NORMAL"
    ? "Todos os pontos estão normais"
    : statuses[worst].label + " em " + names;
  detail.textContent = worst === "NORMAL"
    ? "Nenhum ponto monitorado pede cuidado agora. Volte aqui para ver novos avisos."
    : latestAlert?.descricao ?? "Fique atento e evite áreas alagadas.";
  shareMessage = "Alerta Enchentes Itajubá · " + title.textContent + ". " + detail.textContent
    + " Em emergência, ligue 199 (Defesa Civil).";
  byId("banner-share").href = whatsapp(shareMessage);
  const time = byId("banner-time");
  time.hidden = !latestUpdate;
  if (latestUpdate) {
    time.dateTime = latestUpdate;
    time.textContent = "Última atualização " + when(latestUpdate);
  }
  actions.hidden = false;
}

function renderPoints() {
  const points = [...statusData.pontos].sort((first, second) =>
    statuses[second.status].rank - statuses[first.status].rank || first.bairro.localeCompare(second.bairro, "pt-BR"));
  byId("pontos").replaceChildren(...points.map((point) => {
    const status = statuses[point.status];
    const body = node("div", { class: "body" },
      node("p", { class: "bairro" }, point.bairro),
      node("p", { class: "local" }, point.ponto),
      node("span", { class: "pill" }, status.label),
      node("p", { class: "stamp" }, "Atualizado " + when(point.ts)));

    if (teamSession.authenticated) {
      const select = node("select", { id: "set-" + point.id, "aria-label": "Alterar status de " + point.bairro },
        ...Object.entries(statuses).map(([value, item]) => node("option", { value }, item.label)));
      select.value = point.status;
      select.addEventListener("change", () => updatePoint(point, select));
      body.append(node("label", { class: "team-set", for: select.id }, "Alterar status (equipe)", select));
    }

    const gauge = node("div", { class: "staff", role: "img", "aria-label": "Régua do rio: " + status.label }, node("i"));
    gauge.style.setProperty("--lvl", status.level);
    return node("article", { class: "ponto", "data-s": status.key }, gauge, body);
  }));
}

function renderAlerts() {
  const alerts = statusData.alertas.slice(0, 12).map((alert) => {
    const level = alertLevels[alert.nivel];
    const date = when(alert.ts);
    const message = "Alerta Enchentes Itajubá · " + date + " · " + level.label + " em "
      + alert.bairro + ". " + alert.descricao + " Em emergência, ligue 199.";
    return node("li", { class: "ev", "data-s": statuses[level.status].key },
      node("div", { class: "ev-meta" },
        node("time", { datetime: alert.ts }, date),
        node("span", { class: "chip" }, level.label),
        node("strong", null, alert.bairro)),
      node("p", null, alert.descricao),
      node("a", { class: "share", href: whatsapp(message), target: "_blank", rel: "noopener" }, "Enviar no WhatsApp"));
  });
  byId("timeline").replaceChildren(...(alerts.length ? alerts : [
    node("li", { class: "empty" }, "Nenhum aviso publicado ainda."),
  ]));
}

function renderReports() {
  const reports = statusData.relatos.slice(0, 8).map((report) => {
    const validPhoto = /^data:image\/(jpeg|png|gif|webp);base64,/i.test(report.foto ?? "")
      && report.foto.length <= maxPhotoLength;
    return node("li", null,
      node("div", { class: "meta" },
        node("span", { class: "tipo" }, report.tipo || "Relato"),
        node("span", null, report.bairro),
        node("time", { datetime: report.ts }, when(report.ts)),
        report.localOnly ? node("span", { class: "local-tag" }, "salvo só neste aparelho") : null),
      node("p", null, report.descricao),
      validPhoto ? node("img", { src: report.foto, alt: "Foto enviada pelo morador", loading: "lazy" }) : null);
  });
  byId("relatos").replaceChildren(...(reports.length ? reports : [
    node("li", { class: "empty" }, "Nenhum relato ainda. Quem vir água subindo pode ser o primeiro a avisar."),
  ]));
}

function fillNeighborhoods() {
  const neighborhoods = statusData.pontos.map((point) => point.bairro);
  for (const [id, choices] of [["av-bairro", neighborhoods], ["r-bairro", [...neighborhoods, "Outro bairro"]]]) {
    const select = byId(id);
    const previous = select.value;
    const placeholder = node("option", { value: "", disabled: "", selected: "" }, "Selecione o bairro");
    select.replaceChildren(placeholder, ...choices.map((name) => node("option", { value: name }, name)));
    select.value = choices.includes(previous) ? previous : "";
  }
}

async function loadStatus() {
  const request = ++statusRequest;
  try {
    const data = await api("/api/status");
    if (request !== statusRequest) return;
    const signature = JSON.stringify([data.pontos, data.alertas, data.relatos.map((item) => item.id)]);
    if (signature !== statusSignature) {
      statusSignature = signature;
      statusData = data;
      renderBanner();
      renderPoints();
      renderAlerts();
      renderReports();
      fillNeighborhoods();
    }
    byId("pontos-connection").textContent = "";
  } catch {
    if (request !== statusRequest) return;
    if (!statusSignature) renderBanner();
    byId("pontos-connection").textContent = "Não foi possível atualizar a situação dos pontos.";
  }
}

async function loadWeather() {
  let message;
  try {
    const reading = await api("/api/weather");
    message = "Meteorologia · Open-Meteo · " + numberFormat.format(reading.rainMm)
      + " mm de precipitação estimada · probabilidade máx. nas próximas 6h: "
      + reading.next6hMaxProbability + "%. ";
    message += (reading.stale ? "Leitura desatualizada de " : "Coleta de ") + when(reading.fetchedAt)
      + ". " + reading.disclaimer;
  } catch {
    message = "Meteorologia · A última leitura está indisponível no momento.";
  }
  const weather = byId("weather");
  if (weather.textContent !== message) weather.textContent = message;
}

async function loadSession() {
  const session = await api("/api/admin/session");
  const changed = teamSession.authenticated !== session.authenticated;
  teamSession = session;
  byId("admin-login").hidden = session.authenticated;
  byId("admin").hidden = !session.authenticated;
  if (changed) renderPoints();
}

async function showTeamError(id, error) {
  if ([401, 403].includes(error.status)) {
    await loadSession().catch(() => {});
  }
  note(id, error.message, "err");
}

async function updatePoint(point, select) {
  select.disabled = true;
  try {
    const update = await api("/api/admin/pontos/" + encodeURIComponent(point.id),
      jsonRequest("PATCH", { status: select.value }, teamSession.csrfToken));
    await loadStatus();
    note("pontos-msg", point.bairro + " agora está como " + statuses[update.status].label + ".", "ok");
  } catch (error) {
    select.value = point.status;
    await showTeamError("pontos-msg", error);
  } finally {
    select.disabled = false;
  }
}

byId("form-login").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector("button");
  button.disabled = true;
  try {
    await loadSession();
    await api("/api/admin/login", {
      method: "POST",
      headers: { "X-CSRF-TOKEN": teamSession.csrfToken },
      body: new URLSearchParams({ email: byId("login-email").value.trim(), password: byId("login-password").value }),
    });
    await loadSession();
    form.reset();
    note("login-msg", "");
  } catch (error) {
    note("login-msg", error.status === 401 ? "E-mail ou senha incorretos." : error.message, "err");
  } finally {
    button.disabled = false;
  }
});

byId("logout").addEventListener("click", async (event) => {
  const button = event.currentTarget;
  button.disabled = true;
  try {
    await api("/api/admin/logout", { method: "POST", headers: { "X-CSRF-TOKEN": teamSession.csrfToken } });
    await loadSession();
    note("pontos-msg", "");
    note("av-msg", "");
  } catch (error) {
    await showTeamError("av-msg", error);
  } finally {
    button.disabled = false;
  }
});

byId("form-aviso").addEventListener("submit", async (event) => {
  event.preventDefault();
  const description = byId("av-desc");
  const button = byId("av-btn");
  const bairro = byId("av-bairro").value;
  if (!description.value.trim()) {
    note("av-msg", "Escreva a mensagem do aviso.", "err");
    return;
  }
  button.disabled = true;
  try {
    await api("/api/admin/alertas", jsonRequest("POST", {
      nivel: byId("av-nivel").value.toUpperCase(), bairro, descricao: description.value.trim(),
    }, teamSession.csrfToken));
    description.value = "";
    await loadStatus();
    note("av-msg", "Aviso publicado e situação de " + bairro + " atualizada.", "ok");
  } catch (error) {
    await showTeamError("av-msg", error);
  } finally {
    button.disabled = false;
  }
});

function updateReportButton() {
  byId("r-btn").disabled = photoLoading || reportSubmitting;
}

byId("r-foto").addEventListener("change", async (event) => {
  const input = event.currentTarget;
  const preview = byId("r-prev");
  const request = ++photoRequest;
  const file = input.files[0];
  photo = null;
  preview.hidden = true;
  preview.removeAttribute("src");
  note("r-msg", "");
  photoLoading = Boolean(file);
  updateReportButton();
  if (!file) return;

  const url = URL.createObjectURL(file);
  try {
    const image = new Image();
    image.src = url;
    await image.decode();
    const scale = Math.min(1, 640 / Math.max(image.naturalWidth, image.naturalHeight));
    const canvas = document.createElement("canvas");
    canvas.width = Math.max(1, Math.round(image.naturalWidth * scale));
    canvas.height = Math.max(1, Math.round(image.naturalHeight * scale));
    canvas.getContext("2d").drawImage(image, 0, 0, canvas.width, canvas.height);
    let encoded = canvas.toDataURL("image/jpeg", 0.65);
    if (encoded.length > maxPhotoLength) encoded = canvas.toDataURL("image/jpeg", 0.4);
    if (encoded.length > maxPhotoLength) throw new Error("Imagem muito grande");
    if (request !== photoRequest) return;
    photo = encoded;
    preview.src = photo;
    preview.hidden = false;
  } catch {
    if (request !== photoRequest) return;
    input.value = "";
    note("r-msg", "Não foi possível usar esta foto. Escolha outra imagem ou envie sem foto.", "err");
  } finally {
    URL.revokeObjectURL(url);
    if (request === photoRequest) {
      photoLoading = false;
      updateReportButton();
    }
  }
});

byId("form-relato").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const descricao = byId("r-desc").value.trim();
  if (photoLoading || reportSubmitting) return;
  if (!descricao) {
    note("r-msg", "Descreva o que você está vendo.", "err");
    return;
  }

  reportSubmitting = true;
  byId("r-foto").disabled = true;
  updateReportButton();
  try {
    await api("/api/relatos", jsonRequest("POST", {
      bairro: byId("r-bairro").value, tipo: byId("r-tipo").value, descricao, foto: photo,
    }));
    form.reset();
    photo = null;
    byId("r-prev").hidden = true;
    byId("r-prev").removeAttribute("src");
    await loadStatus();
    note("r-msg", "Relato enviado. Obrigado por avisar.", "ok");
  } catch {
    note("r-msg", "Não foi possível confirmar o envio. Seu relato continua no formulário.", "err");
  } finally {
    reportSubmitting = false;
    byId("r-foto").disabled = false;
    updateReportButton();
  }
});

byId("banner-copy").addEventListener("click", async (event) => {
  const button = event.currentTarget;
  try {
    await navigator.clipboard.writeText(shareMessage);
    button.textContent = "Mensagem copiada";
  } catch {
    button.textContent = "Não foi possível copiar";
  }
  window.setTimeout(() => { button.textContent = "Copiar mensagem"; }, 2200);
});

fillNeighborhoods();
loadStatus();
loadWeather();
loadSession().catch(() => {});
window.setInterval(() => {
  if (document.visibilityState === "visible") {
    loadStatus();
    loadWeather();
  }
}, 60_000);
