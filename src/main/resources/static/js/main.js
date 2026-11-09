// Alerta Enchentes - Frontend JavaScript
// Interações do lado do cliente para a página principal

(function() {
    'use strict';

    // Estado da aplicação
    const state = {
        isAdmin: false,
        adminToken: '',
        showAdminLogin: false,
        localRelatos: [],
        pontosMsg: { text: '', kind: '' },
        avisoMsg: { text: '', kind: '' },
        relatoMsg: { text: '', kind: '' },
        adminLoginMsg: { text: '', kind: '' }
    };

    // Elementos DOM
    const elements = {};

    // Inicialização
    document.addEventListener('DOMContentLoaded', function() {
        cacheElements();
        loadLocalRelatos();
        checkAdminSession();
        setupEventListeners();
        setupAdminModal();
    });

    function cacheElements() {
        elements.adminLoginBtn = document.getElementById('admin-login-btn');
        elements.adminLogoutBtn = document.getElementById('admin-logout-btn');
        elements.adminModal = document.getElementById('admin-modal');
        elements.adminLoginForm = document.getElementById('admin-login-form');
        elements.avisoForm = document.getElementById('form-aviso');
        elements.relatoForm = document.getElementById('form-relato');
        elements.pontoSelects = document.querySelectorAll('.team-set select');
        elements.pontosMsg = document.getElementById('pontos-msg');
        elements.avisoMsg = document.getElementById('av-msg');
        elements.relatoMsg = document.getElementById('r-msg');
        elements.adminLoginMsg = document.getElementById('admin-login-msg');
        elements.fileInput = document.getElementById('r-foto');
        elements.filePreview = document.getElementById('r-prev');
    }

    function loadLocalRelatos() {
        try {
            const stored = localStorage.getItem('alerta-enchentes-relatos');
            state.localRelatos = stored ? JSON.parse(stored) : [];
        } catch (e) {
            state.localRelatos = [];
        }
    }

    function saveLocalRelato(relato) {
        const list = [relato, ...state.localRelatos].slice(0, 10);
        state.localRelatos = list;
        try {
            localStorage.setItem('alerta-enchentes-relatos', JSON.stringify(list));
        } catch (e) {}
    }

    function checkAdminSession() {
        try {
            const token = sessionStorage.getItem('alerta-admin-token');
            if (token) {
                state.adminToken = token;
                verifyAdminToken(token);
            }
        } catch (e) {}
    }

    async function verifyAdminToken(token) {
        try {
            const res = await fetch('/api/admin/session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token })
            });
            if (res.ok) {
                state.isAdmin = true;
                updateAdminUI();
            } else {
                sessionStorage.removeItem('alerta-admin-token');
            }
        } catch (e) {
            sessionStorage.removeItem('alerta-admin-token');
        }
    }

    function updateAdminUI() {
        const adminElements = document.querySelectorAll('[data-admin]');
        adminElements.forEach(el => {
            el.style.display = state.isAdmin ? 'block' : 'none';
        });
        if (elements.adminLoginBtn) {
            elements.adminLoginBtn.style.display = state.isAdmin ? 'none' : 'inline-flex';
        }
        if (elements.adminLogoutBtn) {
            elements.adminLogoutBtn.style.display = state.isAdmin ? 'inline-flex' : 'none';
        }
    }

    function setupEventListeners() {
        // Admin login button
        if (elements.adminLoginBtn) {
            elements.adminLoginBtn.addEventListener('click', () => {
                state.showAdminLogin = true;
                showAdminModal();
            });
        }

        // Admin logout
        if (elements.adminLogoutBtn) {
            elements.adminLogoutBtn.addEventListener('click', handleLogout);
        }

        // Admin login form
        if (elements.adminLoginForm) {
            elements.adminLoginForm.addEventListener('submit', handleAdminLogin);
        }

        // Aviso form (admin)
        if (elements.avisoForm) {
            elements.avisoForm.addEventListener('submit', handleSubmitAviso);
        }

        // Relato form
        if (elements.relatoForm) {
            elements.relatoForm.addEventListener('submit', handleSubmitRelato);
        }

        // Ponto status selects (admin)
        elements.pontoSelects.forEach(select => {
            select.addEventListener('change', (e) => {
                const pontoId = e.target.id.replace('set-', '');
                const ponto = findPontoById(pontoId);
                if (ponto) {
                    setStatusPonto(ponto, e.target.value);
                }
            });
        });

        // File input preview
        if (elements.fileInput) {
            elements.fileInput.addEventListener('change', handleFileChange);
        }

        // Modal overlay click to close
        if (elements.adminModal) {
            elements.adminModal.querySelector('.modal-overlay').addEventListener('click', (e) => {
                if (e.target === e.currentTarget) {
                    hideAdminModal();
                }
            });
        }
    }

    function setupAdminModal() {
        // Modal já está no HTML, só precisamos referenciar
    }

    function showAdminModal() {
        if (elements.adminModal) {
            elements.adminModal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }

    function hideAdminModal() {
        if (elements.adminModal) {
            elements.adminModal.style.display = 'none';
            document.body.style.overflow = '';
            state.showAdminLogin = false;
            state.adminToken = '';
            if (elements.adminLoginForm) {
                elements.adminLoginForm.reset();
            }
            setAdminLoginMsg({ text: '' });
        }
    }

    async function handleAdminLogin(e) {
        e.preventDefault();
        setAdminLoginMsg({ text: '' });
        const token = state.adminToken.trim();
        if (!token) {
            setAdminLoginMsg({ text: 'Informe a senha.', kind: 'err' });
            return;
        }

        try {
            const res = await fetch('/api/admin/session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token })
            });
            if (res.ok) {
                sessionStorage.setItem('alerta-admin-token', token);
                state.isAdmin = true;
                state.showAdminLogin = false;
                state.adminToken = '';
                setAdminLoginMsg({ text: 'Acesso liberado.', kind: 'ok' });
                hideAdminModal();
                updateAdminUI();
                location.reload(); // Recarregar para mostrar painéis admin
            } else {
                const data = await res.json();
                throw new Error(data.error || 'Senha incorreta');
            }
        } catch (err) {
            setAdminLoginMsg({ text: err.message, kind: 'err' });
        }
    }

    function handleLogout() {
        sessionStorage.removeItem('alerta-admin-token');
        state.isAdmin = false;
        state.adminToken = '';
        updateAdminUI();
        location.reload();
    }

    async function handleSubmitAviso(e) {
        e.preventDefault();
        setAvisoMsg({ text: '' });

        const formData = new FormData(e.target);
        const nivel = formData.get('nivel');
        const bairro = formData.get('bairro');
        const descricao = formData.get('descricao');

        if (!descricao.trim()) {
            setAvisoMsg({ text: 'Escreva a mensagem do aviso.', kind: 'err' });
            return;
        }

        try {
            const res = await fetch('/api/admin/alertas', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${state.adminToken}`
                },
                body: JSON.stringify({ nivel, bairro, descricao })
            });
            if (res.ok) {
                e.target.reset();
                setAvisoMsg({ text: `Aviso publicado e status de ${bairro} atualizado.`, kind: 'ok' });
                setTimeout(() => location.reload(), 1500);
            } else {
                const data = await res.json();
                throw new Error(data.error || 'Falha ao publicar');
            }
        } catch (err) {
            setAvisoMsg({ text: err.message, kind: 'err' });
        }
    }

    async function handleSubmitRelato(e) {
        e.preventDefault();
        setRelatoMsg({ text: '' });

        const formData = new FormData(e.target);
        const bairro = formData.get('bairro');
        const tipo = formData.get('tipo');
        const descricao = formData.get('descricao');
        const foto = formData.get('foto');

        if (!descricao.trim()) {
            setRelatoMsg({ text: 'Descreva o que você está vendo.', kind: 'err' });
            return;
        }

        const rec = { bairro, tipo, descricao: descricao.trim() };
        if (foto && foto.size > 0) {
            // Para upload de arquivo, precisamos de FormData
            const uploadFormData = new FormData();
            uploadFormData.append('bairro', bairro);
            uploadFormData.append('tipo', tipo);
            uploadFormData.append('descricao', descricao.trim());
            if (foto) uploadFormData.append('foto', foto);

            try {
                const res = await fetch('/api/relatos', {
                    method: 'POST',
                    body: uploadFormData
                });
                if (res.ok) {
                    const saved = await res.json();
                    setRelatoMsg({ text: 'Relato enviado. Obrigado por avisar.', kind: 'ok' });
                    e.target.reset();
                    if (elements.filePreview) elements.filePreview.style.display = 'none';
                    setTimeout(() => location.reload(), 1500);
                } else throw new Error((await res.json()).error || 'Falha ao enviar');
            } catch (err) {
                const recWithTs = { ...rec, id: crypto.randomUUID(), ts: new Date().toISOString(), local: true };
                saveLocalRelato(recWithTs);
                setRelatoMsg({ text: 'Servidor indisponível. O relato ficou salvo só neste aparelho.', kind: 'err' });
            }
        } else {
            try {
                const res = await fetch('/api/relatos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(rec)
                });
                if (res.ok) {
                    const saved = await res.json();
                    setRelatoMsg({ text: 'Relato enviado. Obrigado por avisar.', kind: 'ok' });
                    e.target.reset();
                    setTimeout(() => location.reload(), 1500);
                } else throw new Error((await res.json()).error || 'Falha ao enviar');
            } catch (err) {
                const recWithTs = { ...rec, id: crypto.randomUUID(), ts: new Date().toISOString(), local: true };
                saveLocalRelato(recWithTs);
                setRelatoMsg({ text: 'Servidor indisponível. O relato ficou salvo só neste aparelho.', kind: 'err' });
            }
        }
    }

    function findPontoById(id) {
        // Busca no DOM ou estado - simplificado
        const card = document.querySelector(`[data-ponto-id="${id}"]`);
        if (card) {
            return {
                id: id,
                bairro: card.querySelector('.bairro')?.textContent || ''
            };
        }
        return null;
    }

    async function setStatusPonto(ponto, newStatus) {
        setPontosMsg({ text: '' });
        try {
            const res = await fetch(`/api/admin/pontos/${encodeURIComponent(ponto.id)}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${state.adminToken}`
                },
                body: JSON.stringify({ status: newStatus })
            });
            if (res.ok) {
                const updated = await res.json();
                setPontosMsg({ text: `${ponto.bairro} agora está como ${getStatusLabel(newStatus)}.`, kind: 'ok' });
                setTimeout(() => location.reload(), 1500);
            } else throw new Error((await res.json()).error || 'Falha ao atualizar');
        } catch (err) {
            setPontosMsg({ text: err.message, kind: 'err' });
        }
    }

    function getStatusLabel(status) {
        const labels = { 'NORMAL': 'Normal', 'ATENCAO': 'Atenção', 'VERMELHO': 'Alerta vermelho' };
        return labels[status] || status;
    }

    function handleFileChange(e) {
        const file = e.target.files?.[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => {
            if (elements.filePreview) {
                elements.filePreview.src = reader.result;
                elements.filePreview.style.display = 'block';
            }
        };
        reader.readAsDataURL(file);
    }

    // Helpers para mensagens
    function setPontosMsg(msg) {
        state.pontosMsg = msg;
        if (elements.pontosMsg) {
            elements.pontosMsg.textContent = msg.text;
            elements.pontosMsg.className = `msg ${msg.kind || ''}`;
        }
    }

    function setAvisoMsg(msg) {
        state.avisoMsg = msg;
        if (elements.avisoMsg) {
            elements.avisoMsg.textContent = msg.text;
            elements.avisoMsg.className = `msg ${msg.kind || ''}`;
        }
    }

    function setRelatoMsg(msg) {
        state.relatoMsg = msg;
        if (elements.relatoMsg) {
            elements.relatoMsg.textContent = msg.text;
            elements.relatoMsg.className = `msg ${msg.kind || ''}`;
        }
    }

    function setAdminLoginMsg(msg) {
        state.adminLoginMsg = msg;
        if (elements.adminLoginMsg) {
            elements.adminLoginMsg.textContent = msg.text;
            elements.adminLoginMsg.className = `msg ${msg.kind || ''}`;
        }
    }

    // Expor funções globais para uso inline se necessário
    window.AlertaEnchentes = {
        setStatusPonto,
        hideAdminModal
    };

})();