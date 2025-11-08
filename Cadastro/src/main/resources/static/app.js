/* app.js — front-end mínimo para testar APIs do backend
   Funcionalidades:
   - login via /api/auth (POST {email, senha})
   - armazenar token em localStorage
   - carregar /api/cliente e /api/animal (GET) e renderizar tabelas

   Ajuste os endpoints conforme sua API real (nomes/paths). */

const loginForm = document.getElementById('loginForm');
const loginMsg = document.getElementById('loginMsg');
const authSection = document.getElementById('auth');
const appSection = document.getElementById('app');
const dataArea = document.getElementById('dataArea');
const btnLogout = document.getElementById('btnLogout');
const btnLoadClientes = document.getElementById('btnLoadClientes');
const btnLoadAnimais = document.getElementById('btnLoadAnimais');
const createForm = document.getElementById('createForm');
const createMsg = document.getElementById('createMsg');
const loginBanner = document.getElementById('loginBanner');

const API_ROOT = '';

function setAuthUI(loggedIn){
  if(loggedIn){
    authSection.classList.add('hidden');
    appSection.classList.remove('hidden');
    // enable form controls when authenticated
    try{ enableCreateForm(true); } catch(e){}
  } else {
    authSection.classList.remove('hidden');
    appSection.classList.add('hidden');
    try{ enableCreateForm(false); } catch(e){}
  }
}

function getToken(){
  return localStorage.getItem('jwt_token');
}

function saveToken(t){
  localStorage.setItem('jwt_token', t);
}

function saveEmail(e){
  try{ localStorage.setItem('jwt_email', e); } catch(e2){}
}

function getEmail(){
  try{ return localStorage.getItem('jwt_email') || ''; } catch(e){ return ''; }
}

function clearEmail(){
  try{ localStorage.removeItem('jwt_email'); } catch(e){}
}

function clearToken(){
  localStorage.removeItem('jwt_token');
}

async function login(event){
  event.preventDefault();
  loginMsg.textContent = '';
  const email = document.getElementById('email').value;
  const senha = document.getElementById('senha').value;

  try{
    const res = await fetch(API_ROOT + '/api/auth/login', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({email, senha})
    });

    if(!res.ok){
      const txt = await res.text();
      loginMsg.textContent = 'Erro: ' + (txt || res.status);
      return;
    }

    const data = await res.json();
    // assume { token: '...' } ou { accessToken: '...' } — tente resolver
    const token = data.token || data.accessToken || data.jwt || data;
    if(!token){
      loginMsg.textContent = 'Resposta sem token';
      return;
    }
    saveToken(token);
    setAuthUI(true);
    // persist small user info to show banner across reloads
    try{ saveEmail(email); setLoginBanner('Logado como: ' + email); } catch(e){}
    // show immediate confirmation both in banner and loginMsg
    loginMsg.style.color = 'green';
    loginMsg.textContent = 'Login efetuado com sucesso como ' + email;
    // clear the loginMsg after a few seconds while banner remains
    setTimeout(() => { loginMsg.textContent = ''; loginMsg.style.color = ''; }, 4000);
    dataArea.innerHTML = '<p>Autenticado. Carregue dados com os botões.</p>';
  }catch(err){
    console.error(err);
    loginMsg.textContent = 'Erro na requisição';
  }
}

async function fetchWithAuth(path, options = {}){
  const token = getToken();
  options.headers = options.headers || {};
  options.headers['Accept'] = 'application/json';
  if(token){
    options.headers['Authorization'] = 'Bearer ' + token;
  }
  const res = await fetch(API_ROOT + path, options);
  if(res.status === 401){
    // token inválido
    clearToken();
    setAuthUI(false);
    throw new Error('Não autorizado (401)');
  }
  return res;
}

async function loadClientes(){
  dataArea.innerHTML = '<p>Carregando clientes...</p>';
  try{
    const res = await fetchWithAuth('/api/clientes');
    if(!res.ok){
      dataArea.innerHTML = '<p>Erro: ' + res.status + '</p>';
      return;
    }
    const list = await res.json();
    // map fields from backend (id_cliente, nome, cpf, telefone)
    renderTable(list, ['id_cliente','nome','cpf','telefone'], 'Clientes');
  }catch(err){
    dataArea.innerHTML = '<p>Erro: ' + err.message + '</p>';
  }
}

async function loadAnimais(){
  dataArea.innerHTML = '<p>Carregando animais...</p>';
  try{
    const res = await fetchWithAuth('/api/animais');
    if(!res.ok){
      dataArea.innerHTML = '<p>Erro: ' + res.status + '</p>';
      return;
    }
    const list = await res.json();
    renderTable(list, ['id','nome','especie','donoId'], 'Animais');
  }catch(err){
    dataArea.innerHTML = '<p>Erro: ' + err.message + '</p>';
  }
}

function renderTable(items, cols, title){
  if(!Array.isArray(items)){
    dataArea.innerHTML = '<pre>' + JSON.stringify(items, null, 2) + '</pre>';
    return;
  }
  let html = '<h3>' + title + '</h3>';
  if(items.length === 0){
    html += '<p>Lista vazia.</p>';
  } else {
    html += '<table><thead><tr>' + cols.map(c => '<th>' + c + '</th>').join('') + '</tr></thead><tbody>';
    for(const it of items){
      html += '<tr>' + cols.map(c => '<td>' + (it[c] !== undefined ? escapeHtml(String(it[c])) : '') + '</td>').join('') + '</tr>';
    }
    html += '</tbody></table>';
  }
  dataArea.innerHTML = html;
}

function escapeHtml(s){
  return s.replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[ch]);
}

function logout(){
  clearToken();
  setAuthUI(false);
  try{ clearEmail(); setLoginBanner(''); } catch(e){}
}

async function createCliente(event){
  event.preventDefault();
  if(!getToken()){
    createMsg.textContent = 'Por favor faça login antes de cadastrar.';
    return;
  }
  createMsg.textContent = '';
  const payload = {
    nome_cliente: document.getElementById('nome_cliente').value,
    cpf: document.getElementById('cpf').value,
    telefone: document.getElementById('telefone').value,
    nome_animal: document.getElementById('nome_animal').value,
    especie: document.getElementById('especie').value,
    raca: document.getElementById('raca').value
  };

  try{
    const res = await fetchWithAuth('/api/clientes', {
      method: 'POST',
      headers: {'Content-Type': 'application/json; charset=utf-8'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){
      const t = await res.text();
      createMsg.textContent = 'Erro: ' + (t || res.status);
      return;
    }
    const data = await res.json();
    createMsg.textContent = 'Criado: clienteId=' + data.clienteId + ' animalId=' + data.animalId;
    // refresh clients list
    await loadClientes();
    createForm.reset();
  }catch(err){
    console.error(err);
    createMsg.textContent = 'Erro na requisição: ' + err.message;
  }
}

// event handlers
loginForm.addEventListener('submit', login);
btnLogout.addEventListener('click', logout);
btnLoadClientes.addEventListener('click', loadClientes);
btnLoadAnimais.addEventListener('click', loadAnimais);
if(createForm){ createForm.addEventListener('submit', createCliente); }

// init
setAuthUI(!!getToken());
// reflect current auth state on form controls
try{ enableCreateForm(!!getToken()); } catch(e){}
// show banner if we have stored email
try{ const e = getEmail(); if(e){ setLoginBanner('Logado como: ' + e); } } catch(e){}

function setLoginBanner(text){
  if(!loginBanner) return;
  loginBanner.textContent = text || '';
}

function enableCreateForm(enable){
  if(!createForm) return;
  const controls = createForm.querySelectorAll('input,button');
  controls.forEach(c => { c.disabled = !enable; });
}

/* Observações:
 - Ajuste os paths '/api/auth/login', '/api/clientes', '/api/animais' se necessário.
 - Se a sua API retornar token de outra chave, altere a captura acima.
*/