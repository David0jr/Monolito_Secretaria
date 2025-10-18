document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.querySelectorAll('.nav-link');
    const mainContentArea = document.getElementById('main-content-area');
    const logoutButton = document.getElementById('logout-button');

    // Base URL da API (pode ser configurado para diferentes ambientes)
    const API_BASE_URL = 'http://localhost:8080/api';

    // Objeto de mapeamento de endpoints para funções de renderização
    const endpoints = {
        '/home': renderHomeContent,
        '/cursos': fetchAndRenderCursos,
        '/disciplinas': fetchAndRenderDisciplinas,
        '/turmas': fetchAndRenderTurmas,
        '/professores': fetchAndRenderProfessores,
        '/alunos': fetchAndRenderAlunos
    };

    // Adiciona event listeners aos links de navegação
    navLinks.forEach(link => {
        link.addEventListener('click', async (event) => {
            event.preventDefault();

            // Remove a classe 'active' de todos os links e adiciona ao clicado
            navLinks.forEach(item => item.classList.remove('active'));
            event.target.classList.add('active');

            const href = event.target.getAttribute('href');

            // Exibe "Carregando..." imediatamente para todos os cliques nos links de navegação
            mainContentArea.innerHTML = '<h2>Carregando...</h2>';

            // Chama a função correspondente ao endpoint
            if (endpoints[href]) {
                await endpoints[href]();
            } else if (href === '/home') {
                renderHomeContent();
            }
        });
    });

    // --- FUNÇÕES GERAIS / UTILITIES ---

    // Função para renderizar o conteúdo inicial
    function renderHomeContent() {
        mainContentArea.innerHTML = `
            <h1>Bem-vindo(a), Secretaria!</h1>
            <p>Selecione uma opção no menu lateral para visualizar seu conteúdo.</p>
        `;
    }

    // Função para buscar dados da API com tratamento de erros
    async function fetchData(url, options = {}) {
        try {
            const response = await fetch(url, options);

            if (!response.ok) {
                let errorMessage = `Erro ao carregar dados: ${response.status} ${response.statusText}`;

                // Tenta obter mensagem de erro do corpo da resposta
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    // Se não for JSON, usa o texto simples
                    const errorText = await response.text();
                    if (errorText) errorMessage = errorText;
                }

                throw new Error(errorMessage);
            }

            // Para respostas sem conteúdo (como DELETE)
            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            mainContentArea.innerHTML = `<p class="text-danger">Erro: ${error.message}</p>`;
            console.error('Erro em fetchData:', error);
            throw error;
        }
    }

    // FUNÇÃO UTILITÁRIA para fetch com CSRF
    async function fetchWithCSRF(url, options = {}) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...(csrfToken && csrfHeader && { [csrfHeader]: csrfToken })
            }
        };

        const mergedOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        return await fetchData(url, mergedOptions);
    }

    // Função utilitária para buscar e popular selects (para 1 seleção)
    async function fetchAndPopulateSelect(url, selectId, displayField, selectedId = null) {
        const selectElement = document.getElementById(selectId);
        if (!selectElement) {
            return;
        }

        selectElement.innerHTML = '<option value="">Carregando...</option>';
        try {
            const data = await fetchData(url);
            selectElement.innerHTML = `<option value="">Selecione um(a) ${displayField}</option>`;

            if (data && data.length > 0) {
                data.forEach(item => {
                    const option = document.createElement('option');
                    option.value = item.id;
                    option.textContent = item[displayField];
                    if (selectedId !== null && item.id == selectedId) {
                        option.selected = true;
                    }
                    selectElement.appendChild(option);
                });
            } else {
                selectElement.innerHTML = `<option value="">Nenhum(a) ${displayField} disponível</option>`;
            }
        } catch (error) {
            selectElement.innerHTML = `<option value="">Erro ao carregar ${displayField}</option>`;
            console.error(`Erro ao carregar dados para o select '${selectId}':`, error);
        }
    }

    // Nova função para popular selects múltiplos
    async function fetchAndPopulateMultiSelect(url, selectId, displayField, selectedIds = []) {
        const selectElement = document.getElementById(selectId);
        if (!selectElement) {
            return;
        }

        selectElement.innerHTML = '<option value="">Carregando...</option>';
        try {
            const data = await fetchData(url);
            selectElement.innerHTML = '';

            if (data && data.length > 0) {
                data.forEach(item => {
                    const option = document.createElement('option');
                    option.value = item.id;
                    option.textContent = item[displayField];
                    if (selectedIds.includes(item.id)) {
                        option.selected = true;
                    }
                    selectElement.appendChild(option);
                });
            } else {
                selectElement.innerHTML = `<option value="">Nenhum(a) ${displayField} disponível</option>`;
            }
        } catch (error) {
            selectElement.innerHTML = `<option value="">Erro ao carregar ${displayField}</option>`;
            console.error(`Erro ao carregar dados para o select múltiplo '${selectId}':`, error);
        }
    }

    // --- FUNÇÕES DE CRUD (TABELAS) ---

    // CURSOS
    async function fetchAndRenderCursos() {
        mainContentArea.innerHTML = '<h2>Gerenciar Cursos</h2><p>Carregando cursos...</p>';
        try {
            const cursos = await fetchData(`${API_BASE_URL}/cursos`);
            if (!cursos || cursos.length === 0) {
                mainContentArea.innerHTML = `<h2>Gerenciar Cursos</h2><p>Nenhum curso encontrado.</p>
                                         <button id="addCursoBtn" class="btn btn-primary mb-3">Adicionar Curso</button>`;
                document.getElementById('addCursoBtn')?.addEventListener('click', () => showCursoForm());
                return;
            }

            let htmlContent = '<h2>Gerenciar Cursos</h2>';
            htmlContent += `<button id="addCursoBtn" class="btn btn-primary mb-3">Adicionar Curso</button>`;
            htmlContent += `<table class="table table-striped"><thead><tr><th>ID</th><th>Nome</th><th>Descrição</th><th>Disciplinas Associadas</th><th>Ações</th></tr></thead><tbody>`;

            cursos.forEach(curso => {
                const disciplinasNomes = curso.disciplinas && curso.disciplinas.length > 0
                    ? curso.disciplinas.map(d => d.nome).join(', ')
                    : 'Nenhuma';

                htmlContent += `
                <tr>
                    <td>${curso.id}</td>
                    <td>${curso.nome}</td>
                    <td>${curso.descricao}</td>
                    <td>${disciplinasNomes}</td>
                    <td>
                        <button class="btn btn-warning btn-sm edit-btn" data-id="${curso.id}">Editar</button>
                        <button class="btn btn-danger btn-sm delete-btn" data-id="${curso.id}">Excluir</button>
                    </td>
                </tr>
            `;
            });
            htmlContent += `</tbody></table>`;
            mainContentArea.innerHTML = htmlContent;

            const addCursoBtn = document.getElementById('addCursoBtn');
            if (addCursoBtn) addCursoBtn.addEventListener('click', () => showCursoForm());
            document.querySelectorAll('.edit-btn').forEach(btn => btn.addEventListener('click', (e) => showCursoForm(e.target.dataset.id)));
            document.querySelectorAll('.delete-btn').forEach(btn => btn.addEventListener('click', (e) => deleteCurso(e.target.dataset.id)));
        } catch (error) {
            console.error('Erro ao carregar cursos:', error);
            mainContentArea.innerHTML = `<h2>Gerenciar Cursos</h2><p class="text-danger">Erro ao carregar cursos: ${error.message}</p>`;
        }
    }

    async function showCursoForm(id = null) {
        let curso = {};
        try {
            if (id) {
                mainContentArea.innerHTML = `<h2>Editando Curso...</h2><p>Carregando dados do curso...</p>`;
                curso = await fetchData(`${API_BASE_URL}/cursos/${id}`);
            } else {
                mainContentArea.innerHTML = `<h2>Novo Curso</h2>`;
            }

            let disciplinasSection = '';
            if (id && curso.disciplinas) {
                const disciplinasDoCurso = curso.disciplinas.map(d => `<li>${d.nome} (Carga Horária: ${d.cargaHoraria}h)</li>`).join('');
                disciplinasSection = `
                <h3>Disciplinas Deste Curso:</h3>
                <ul class="list-group mb-3">
                    ${disciplinasDoCurso || '<li class="list-group-item">Nenhuma disciplina associada ainda.</li>'}
                </ul>
                <button type="button" class="btn btn-info mb-3" id="addDisciplinaAoCursoBtn" data-curso-id="${curso.id}">Adicionar Nova Disciplina a Este Curso</button>
            `;
            }

            mainContentArea.innerHTML = `
            <h2>${id ? 'Editar Curso' : 'Novo Curso'}</h2>
            <form id="cursoForm">
                <input type="hidden" id="cursoId" value="${curso.id || ''}">
                <div class="mb-3"><label for="nome" class="form-label">Nome</label><input type="text" class="form-control" id="nome" value="${curso.nome || ''}" required></div>
                <div class="mb-3"><label for="descricao" class="form-label">Descrição</label><textarea class="form-control" id="descricao" rows="3">${curso.descricao || ''}</textarea></div>
                <button type="submit" class="btn btn-success">Salvar</button>
                <button type="button" class="btn btn-secondary" id="cancelCursoBtn">Cancelar</button>
            </form>
            <hr>
            ${disciplinasSection}
        `;

            const cursoForm = document.getElementById('cursoForm');
            if (cursoForm) {
                cursoForm.addEventListener('submit', handleCursoSubmit);
            }
            document.getElementById('cancelCursoBtn')?.addEventListener('click', fetchAndRenderCursos);

            const addDisciplinaAoCursoBtn = document.getElementById('addDisciplinaAoCursoBtn');
            if (addDisciplinaAoCursoBtn) {
                addDisciplinaAoCursoBtn.addEventListener('click', (e) => {
                    const cursoId = e.target.dataset.cursoId;
                    showDisciplinaForm(null, cursoId);
                });
            }
        } catch (error) {
            console.error('Erro ao carregar formulário de curso:', error);
            mainContentArea.innerHTML = `<h2>Erro</h2><p class="text-danger">Erro ao carregar formulário: ${error.message}</p>`;
        }
    }

    async function handleCursoSubmit(event) {
        event.preventDefault();
        const id = document.getElementById('cursoId')?.value;
        const url = id ? `${API_BASE_URL}/cursos/${id}` : `${API_BASE_URL}/cursos`;
        const method = id ? 'PUT' : 'POST';

        const cursoData = {
            id: id || null,
            nome: document.getElementById('nome')?.value,
            descricao: document.getElementById('descricao')?.value
        };

        try {
            await fetchWithCSRF(url, {
                method: method,
                body: JSON.stringify(cursoData)
            });

            alert(id ? 'Curso atualizado com sucesso!' : 'Curso cadastrado com sucesso!');
            fetchAndRenderCursos();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao salvar curso: ${error.message}`);
        }
    }

    async function deleteCurso(id) {
        if (!confirm('Tem certeza que deseja deletar este curso? Isso também deletará as disciplinas e turmas associadas a ele!')) return;
        try {
            await fetchWithCSRF(`${API_BASE_URL}/cursos/${id}`, {
                method: 'DELETE'
            });
            alert('Curso deletado com sucesso!');
            fetchAndRenderCursos();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao deletar: ${error.message}`);
        }
    }

    // --- DISCIPLINAS (CRUD) ---
    async function fetchAndRenderDisciplinas() {
        mainContentArea.innerHTML = '<h2>Gerenciar Disciplinas</h2><p>Carregando disciplinas...</p>';

        try {
            const disciplinas = await fetchData(`${API_BASE_URL}/disciplinas`);

            if (!disciplinas || disciplinas.length === 0) {
                mainContentArea.innerHTML = `<h2>Gerenciar Disciplinas</h2><p>Nenhuma disciplina encontrada.</p>
                                     <button id="addDisciplinaBtn" class="btn btn-primary mb-3">Adicionar Disciplina</button>`;
                document.getElementById('addDisciplinaBtn')?.addEventListener('click', () => showDisciplinaForm());
                return;
            }

            let htmlContent = '<h2>Gerenciar Disciplinas</h2>';
            htmlContent += `<button id="addDisciplinaBtn" class="btn btn-primary mb-3">Adicionar Disciplina</button>`;
            htmlContent += `<table class="table table-striped"><thead><tr><th>ID</th><th>Nome</th><th>Carga Horária</th><th>Professor</th><th>Cursos Associados</th><th>Pré-Requisitos</th><th>Ações</th></tr></thead><tbody>`;

            disciplinas.forEach(disciplina => {
                const preRequisitosNomes = disciplina.nomesPreRequisitos && disciplina.nomesPreRequisitos.length > 0
                    ? disciplina.nomesPreRequisitos.join(', ')
                    : 'Nenhum';
                const cursosAssociadosNomes = disciplina.nomesCursos && disciplina.nomesCursos.length > 0
                    ? disciplina.nomesCursos.join(', ')
                    : 'Nenhum';

                htmlContent += `
            <tr>
                <td>${disciplina.id}</td>
                <td>${disciplina.nome}</td>
                <td>${disciplina.cargaHoraria}h</td>
                <td>${disciplina.nomeProfessor || 'Não Atribuído'}</td>
                <td>${cursosAssociadosNomes}</td>
                <td>${preRequisitosNomes}</td>
                <td>
                    <button class="btn btn-warning btn-sm edit-btn" data-id="${disciplina.id}">Editar</button>
                    <button class="btn btn-danger btn-sm delete-btn" data-id="${disciplina.id}">Excluir</button>
                </td>
            </tr>
        `;
            });
            htmlContent += `</tbody></table>`;
            mainContentArea.innerHTML = htmlContent;

            const addDisciplinaBtn = document.getElementById('addDisciplinaBtn');
            if (addDisciplinaBtn) addDisciplinaBtn.addEventListener('click', () => showDisciplinaForm());
            document.querySelectorAll('.edit-btn').forEach(btn => btn.addEventListener('click', (e) => showDisciplinaForm(e.target.dataset.id)));
            document.querySelectorAll('.delete-btn').forEach(btn => btn.addEventListener('click', (e) => deleteDisciplina(e.target.dataset.id)));
        } catch (error) {
            console.error('Erro ao carregar disciplinas:', error);
            mainContentArea.innerHTML = `<h2>Gerenciar Disciplinas</h2>
                                <p class="text-danger">Erro ao carregar disciplinas: ${error.message}</p>
                                <button id="addDisciplinaBtn" class="btn btn-primary mb-3">Adicionar Disciplina</button>`;
            document.getElementById('addDisciplinaBtn')?.addEventListener('click', () => showDisciplinaForm());
        }
    }

    async function showDisciplinaForm(id = null, preSelectedCursoId = null) {
        let disciplina = {};
        let selectedPreRequisitosIds = [];
        let selectedCursosIds = [];

        try {
            if (id) {
                mainContentArea.innerHTML = `<h2>Editando Disciplina...</h2><p>Carregando dados da disciplina...</p>`;
                disciplina = await fetchData(`${API_BASE_URL}/disciplinas/${id}`);
                selectedPreRequisitosIds = disciplina.preRequisitosIds || [];
                selectedCursosIds = disciplina.cursosIds || [];
            } else {
                mainContentArea.innerHTML = `<h2>Nova Disciplina</h2>`;
                if (preSelectedCursoId) {
                    selectedCursosIds = [parseInt(preSelectedCursoId)];
                }
            }

            mainContentArea.innerHTML = `
        <h2>${id ? 'Editar Disciplina' : 'Nova Disciplina'}</h2>
        <form id="disciplinaForm">
            <input type="hidden" id="disciplinaId" value="${disciplina.id || ''}">
            <div class="mb-3">
                <label for="nome" class="form-label">Nome da Disciplina</label>
                <input type="text" class="form-control" id="nome" value="${disciplina.nome || ''}" required>
            </div>
            <div class="mb-3">
                <label for="cargaHoraria" class="form-label">Carga Horária (em horas)</label>
                <input type="number" class="form-control" id="cargaHoraria" value="${disciplina.cargaHoraria || ''}" required>
            </div>
            <div class="mb-3">
                <label for="professorId" class="form-label">Professor</label>
                <select class="form-control" id="professorId" required>
                    <option value="">Carregando professores...</option>
                </select>
            </div>
            <div class="mb-3">
                <label for="cursosIds" class="form-label">Cursos Associados (selecione um ou mais)</label>
                <select class="form-control" id="cursosIds" multiple required>
                    <option value="">Carregando cursos...</option>
                </select>
                <small class="form-text text-muted">Mantenha 'Ctrl' ou 'Cmd' pressionado para selecionar múltiplos.</small>
            </div>

            <h3>Pré-Requisitos:</h3>
            <div id="preRequisitosContainer" class="mb-3">
                <p>Carregando pré-requisitos...</p>
            </div>

            <button type="submit" class="btn btn-success">Salvar</button>
            <button type="button" class="btn btn-secondary" id="cancelDisciplinaBtn">Cancelar</button>
        </form>
    `;

            await fetchAndPopulateSelect(`${API_BASE_URL}/professores`, 'professorId', 'nome', disciplina.professorId);
            await fetchAndPopulateMultiSelect(`${API_BASE_URL}/cursos`, 'cursosIds', 'nome', selectedCursosIds);
            await loadAndPopulatePreRequisitosForDisciplinaForm(id, selectedPreRequisitosIds);

            const disciplinaForm = document.getElementById('disciplinaForm');
            if (disciplinaForm) {
                disciplinaForm.addEventListener('submit', handleDisciplinaSubmit);
            }
            document.getElementById('cancelDisciplinaBtn')?.addEventListener('click', fetchAndRenderDisciplinas);

        } catch (error) {
            console.error('Erro ao carregar formulário de disciplina:', error);
            mainContentArea.innerHTML = `<h2>Erro</h2>
                                <p class="text-danger">Erro ao carregar formulário: ${error.message}</p>
                                <button class="btn btn-secondary" onclick="fetchAndRenderDisciplinas()">Voltar</button>`;
        }
    }

    async function loadAndPopulatePreRequisitosForDisciplinaForm(currentDisciplinaId, selectedPreRequisitosIds) {
        const preRequisitosContainer = document.getElementById('preRequisitosContainer');
        preRequisitosContainer.innerHTML = '<p>Carregando pré-requisitos...</p>';

        try {
            const todasDisciplinas = await fetchData(`${API_BASE_URL}/disciplinas`);
            preRequisitosContainer.innerHTML = '';

            const disciplinasFiltradas = todasDisciplinas.filter(d => d.id !== currentDisciplinaId);

            if (disciplinasFiltradas && disciplinasFiltradas.length > 0) {
                disciplinasFiltradas.forEach(disciplina => {
                    const div = document.createElement('div');
                    const checkbox = document.createElement('input');
                    checkbox.type = 'checkbox';
                    checkbox.id = `pre-requisito-${disciplina.id}`;
                    checkbox.value = disciplina.id;
                    checkbox.name = 'preRequisitos';

                    if (selectedPreRequisitosIds.includes(disciplina.id)) {
                        checkbox.checked = true;
                    }

                    const label = document.createElement('label');
                    label.htmlFor = `pre-requisito-${disciplina.id}`;
                    label.textContent = `${disciplina.nome} (Carga: ${disciplina.cargaHoraria}h, Prof: ${disciplina.nomeProfessor || 'N/A'}, Cursos: ${disciplina.nomesCursos?.join(', ') || 'N/A'})`;
                    label.classList.add('form-check-label');
                    checkbox.classList.add('form-check-input', 'me-2');

                    div.classList.add('form-check');
                    div.appendChild(checkbox);
                    div.appendChild(label);
                    preRequisitosContainer.appendChild(div);
                });
            } else {
                preRequisitosContainer.innerHTML = '<p>Nenhuma outra disciplina disponível para pré-requisito.</p>';
            }
        } catch (error) {
            preRequisitosContainer.innerHTML = `<p class="text-danger">Erro ao carregar pré-requisitos: ${error.message}</p>`;
            console.error('Erro ao carregar pré-requisitos para o formulário de disciplina:', error);
        }
    }

    async function handleDisciplinaSubmit(event) {
        event.preventDefault();
        const id = document.getElementById('disciplinaId')?.value;
        const url = id ? `${API_BASE_URL}/disciplinas/${id}` : `${API_BASE_URL}/disciplinas`;
        const method = id ? 'PUT' : 'POST';

        const disciplinaData = {
            id: id || null,
            nome: document.getElementById('nome')?.value,
            cargaHoraria: parseInt(document.getElementById('cargaHoraria')?.value) || 0,
            professorId: parseInt(document.getElementById('professorId')?.value) || null,
            cursosIds: Array.from(document.getElementById('cursosIds').selectedOptions).map(option => parseInt(option.value)),
            preRequisitosIds: Array.from(document.querySelectorAll('input[name="preRequisitos"]:checked')).map(cb => parseInt(cb.value))
        };

        if (!disciplinaData.professorId || isNaN(disciplinaData.professorId)) {
            alert('Por favor, selecione um professor válido.');
            return;
        }

        if (!disciplinaData.cursosIds || disciplinaData.cursosIds.length === 0) {
            alert('Por favor, selecione pelo menos um curso.');
            return;
        }

        try {
            await fetchWithCSRF(url, {
                method: method,
                body: JSON.stringify(disciplinaData)
            });

            alert('Disciplina salva com sucesso!');
            fetchAndRenderDisciplinas();
        } catch (error) {
            console.error('Erro ao salvar disciplina:', error);
            alert(`Erro ao salvar disciplina: ${error.message}`);
        }
    }

    async function deleteDisciplina(id) {
        if (!confirm('Tem certeza que deseja deletar esta disciplina?')) return;
        try {
            await fetchWithCSRF(`${API_BASE_URL}/disciplinas/${id}`, {
                method: 'DELETE'
            });

            alert('Disciplina deletada com sucesso!');
            fetchAndRenderDisciplinas();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao deletar: ${error.message}`);
        }
    }

    // PROFESSORES
    async function fetchAndRenderProfessores() {
        mainContentArea.innerHTML = '<h2>Gerenciar Professores</h2><p>Carregando professores...</p>';

        try {
            const professores = await fetchData(`${API_BASE_URL}/professores`);

            if (!professores || professores.length === 0) {
                mainContentArea.innerHTML = `
                <h2>Gerenciar Professores</h2>
                <p>Nenhum professor encontrado.</p>
                <button id="addProfessorBtn" class="btn btn-primary mb-3">Adicionar Professor</button>
            `;
                document.getElementById('addProfessorBtn')?.addEventListener('click', () => renderProfessorFormCadastro());
                return;
            }

            let htmlContent = `
            <h2>Gerenciar Professores</h2>
            <button id="addProfessorBtn" class="btn btn-primary mb-3">Adicionar Professor</button>
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Nome Completo</th>
                            <th>Especialização</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

            professores.forEach(prof => {
                htmlContent += `
                <tr>
                    <td>${prof.nome}</td>
                    <td>${prof.especializacao}</td>
                    <td>
                        <button class="btn btn-info btn-sm view-btn" data-id="${prof.id}">Ver Informações</button>
                        <button class="btn btn-warning btn-sm edit-btn" data-id="${prof.id}">Editar</button>
                        <button class="btn btn-danger btn-sm delete-btn" data-id="${prof.id}">Excluir</button>
                    </td>
                </tr>
            `;
            });

            htmlContent += `</tbody></table></div>`;
            mainContentArea.innerHTML = htmlContent;

            document.getElementById('addProfessorBtn')?.addEventListener('click', () => renderProfessorFormCadastro());
            document.querySelectorAll('.view-btn').forEach(btn =>
                btn.addEventListener('click', (e) => showProfessorDetalhes(e.target.dataset.id))
            );
            document.querySelectorAll('.edit-btn').forEach(btn =>
                btn.addEventListener('click', (e) => showProfessorFormEdicao(e.target.dataset.id))
            );
            document.querySelectorAll('.delete-btn').forEach(btn =>
                btn.addEventListener('click', (e) => deleteProfessor(e.target.dataset.id))
            );

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Gerenciar Professores</h2><p class="text-danger">Erro ao carregar professores: ${error.message}</p>`;
        }
    }

    async function showProfessorDetalhes(id) {
        mainContentArea.innerHTML = '<h2>Carregando informações do professor...</h2>';

        try {
            const professor = await fetchData(`${API_BASE_URL}/professores/${id}`);

            let htmlContent = `
            <h2>Informações do Professor</h2>
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h5>Dados Pessoais</h5>
                            <p><strong>Nome Completo:</strong> ${professor.nome}</p>
                            <p><strong>CPF:</strong> ${professor.cpf}</p>
                            <p><strong>Especialização:</strong> ${professor.especializacao}</p>
                            <p><strong>Nome de Usuário:</strong> ${professor.username}</p>
                        </div>
                        <div class="col-md-6">
                            <h5>Informações Profissionais</h5>
                            <p><strong>Total de Disciplinas:</strong> ${professor.disciplinaNomes ? professor.disciplinaNomes.length : 0}</p>
                        </div>
                    </div>
                    <hr>
                    <h5>Disciplinas Ministradas</h5>
                    <div id="disciplinas-professor">
                        ${professor.disciplinaNomes && professor.disciplinaNomes.length > 0
                ? `<ul class="list-group">${professor.disciplinaNomes.map(disc => `<li class="list-group-item">${disc}</li>`).join('')}</ul>`
                : '<p>Nenhuma disciplina atribuída.</p>'
            }
                    </div>
                </div>
            </div>
            <button class="btn btn-secondary mt-3" id="voltarListaBtn">Voltar para Lista</button>
        `;

            mainContentArea.innerHTML = htmlContent;
            document.getElementById('voltarListaBtn')?.addEventListener('click', fetchAndRenderProfessores);

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Erro</h2><p class="text-danger">Erro ao carregar informações do professor: ${error.message}</p>`;
        }
    }

    async function renderProfessorFormCadastro() {
        mainContentArea.innerHTML = `
        <h2>Cadastrar Novo Professor</h2>
        <form id="professorFormCadastro">
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <label for="nome" class="form-label">Nome Completo *</label>
                        <input type="text" class="form-control" id="nome" name="nome" required>
                    </div>
                    <div class="mb-3">
                        <label for="cpf" class="form-label">CPF *</label>
                        <input type="text" class="form-control" id="cpf" name="cpf" maxlength="11" required>
                    </div>
                    <div class="mb-3">
                        <label for="especializacao" class="form-label">Especialização *</label>
                        <input type="text" class="form-control" id="especializacao" name="especializacao" required>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="mb-3">
                        <label for="username" class="form-label">Nome de Usuário *</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Senha *</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                </div>
            </div>
            <button type="submit" class="btn btn-success">Cadastrar Professor</button>
            <button type="button" class="btn btn-secondary" id="cancelCadastroBtn">Cancelar</button>
        </form>
    `;

        const professorForm = document.getElementById('professorFormCadastro');
        if (professorForm) {
            professorForm.addEventListener('submit', handleProfessorCadastroSubmit);
        }

        document.getElementById('cancelCadastroBtn')?.addEventListener('click', fetchAndRenderProfessores);
    }

    async function handleProfessorCadastroSubmit(event) {
        event.preventDefault();

        const formData = new FormData(event.target);
        const professorData = {
            nome: formData.get('nome'),
            cpf: formData.get('cpf'),
            especializacao: formData.get('especializacao'),
            username: formData.get('username'),
            password: formData.get('password')
        };

        try {
            await fetchWithCSRF(`${API_BASE_URL}/professores`, {
                method: 'POST',
                body: JSON.stringify(professorData)
            });

            alert('Professor cadastrado com sucesso!');
            fetchAndRenderProfessores();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao cadastrar professor: ${error.message}`);
        }
    }

    async function showProfessorFormEdicao(id) {
        mainContentArea.innerHTML = '<h2>Editando Professor...</h2><p>Carregando dados do professor...</p>';

        try {
            const professor = await fetchData(`${API_BASE_URL}/professores/${id}`);

            mainContentArea.innerHTML = `
            <h2>Editar Professor</h2>
            <form id="professorFormEdicao">
                <input type="hidden" id="professorId" value="${professor.id}">
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label for="nome" class="form-label">Nome Completo</label>
                            <input type="text" class="form-control" id="nome" value="${professor.nome}" required>
                        </div>
                        <div class="mb-3">
                            <label for="cpf" class="form-label">CPF</label>
                            <input type="text" class="form-control" id="cpf" value="${professor.cpf}" required>
                        </div>
                        <div class="mb-3">
                            <label for="especializacao" class="form-label">Especialização</label>
                            <input type="text" class="form-control" id="especializacao" value="${professor.especializacao}" required>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label for="username" class="form-label">Nome de Usuário</label>
                            <input type="text" class="form-control" id="username" value="${professor.username}" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Nova Senha (Opcional)</label>
                            <input type="password" class="form-control" id="password" placeholder="Deixe vazio para manter a senha atual">
                        </div>
                    </div>
                </div>
                <button type="submit" class="btn btn-success">Salvar Alterações</button>
                <button type="button" class="btn btn-secondary" id="cancelEdicaoBtn">Cancelar</button>
            </form>
        `;

            document.getElementById('professorFormEdicao')?.addEventListener('submit', handleProfessorEdicaoSubmit);
            document.getElementById('cancelEdicaoBtn')?.addEventListener('click', fetchAndRenderProfessores);

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Erro</h2><p class="text-danger">Erro ao carregar dados do professor: ${error.message}</p>`;
        }
    }

    async function handleProfessorEdicaoSubmit(event) {
        event.preventDefault();

        const id = document.getElementById('professorId').value;
        const professorData = {
            id: id,
            nome: document.getElementById('nome').value,
            cpf: document.getElementById('cpf').value,
            especializacao: document.getElementById('especializacao').value,
            username: document.getElementById('username').value,
            password: document.getElementById('password').value || null
        };

        try {
            await fetchWithCSRF(`${API_BASE_URL}/professores/${id}`, {
                method: 'PUT',
                body: JSON.stringify(professorData)
            });
            alert('Professor atualizado com sucesso!');
            fetchAndRenderProfessores();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao atualizar professor: ${error.message}`);
        }
    }

    async function deleteProfessor(id) {
        if (!confirm('Tem certeza que deseja excluir este professor?')) return;

        try {
            await fetchWithCSRF(`${API_BASE_URL}/professores/${id}`, {
                method: 'DELETE'
            });
            alert('Professor excluído com sucesso!');
            fetchAndRenderProfessores();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao excluir professor: ${error.message}`);
        }
    }

    // TURMAS
    async function fetchAndRenderTurmas() {
        mainContentArea.innerHTML = '<h2>Gerenciar Turmas</h2><p>Carregando turmas...</p>';

        try {
            const turmas = await fetchData(`${API_BASE_URL}/turmas`);

            if (!turmas || turmas.length === 0) {
                mainContentArea.innerHTML = `<h2>Gerenciar Turmas</h2><p>Nenhuma turma encontrada.</p>
                                     <button id="addTurmaBtn" class="btn btn-primary mb-3">Adicionar Turma</button>`;
                document.getElementById('addTurmaBtn')?.addEventListener('click', () => showTurmaForm());
                return;
            }

            let htmlContent = '<h2>Gerenciar Turmas</h2>';
            htmlContent += `<button id="addTurmaBtn" class="btn btn-primary mb-3">Adicionar Turma</button>`;
            htmlContent += `<table class="table table-striped"><thead><tr><th>ID</th><th>Período</th><th>Disciplinas</th><th>Professores</th><th>Alunos</th><th>Ações</th></tr></thead><tbody>`;

            turmas.forEach(turma => {
                const disciplinasCount = turma.disciplinasIds ? turma.disciplinasIds.length : 0;
                const professoresCount = turma.professoresIds ? turma.professoresIds.length : 0;
                const alunosCount = turma.alunosIds ? turma.alunosIds.length : 0;

                htmlContent += `
            <tr>
                <td>${turma.id}</td>
                <td>${turma.periodo}</td>
                <td>${disciplinasCount} disciplina(s)</td>
                <td>${professoresCount} professor(es)</td>
                <td>${alunosCount} aluno(s)</td>
                <td>
                    <button class="btn btn-warning btn-sm edit-btn" data-id="${turma.id}">Editar</button>
                    <button class="btn btn-danger btn-sm delete-btn" data-id="${turma.id}">Excluir</button>
                    <button class="btn btn-info btn-sm manage-btn" data-id="${turma.id}">Gerenciar</button>
                </td>
            </tr>
        `;
            });
            htmlContent += `</tbody></table>`;
            mainContentArea.innerHTML = htmlContent;

            document.getElementById('addTurmaBtn')?.addEventListener('click', () => showTurmaForm());
            document.querySelectorAll('.edit-btn').forEach(btn =>
                btn.addEventListener('click', (e) => showTurmaForm(e.target.dataset.id))
            );
            document.querySelectorAll('.delete-btn').forEach(btn =>
                btn.addEventListener('click', (e) => deleteTurma(e.target.dataset.id))
            );
            document.querySelectorAll('.manage-btn').forEach(btn =>
                btn.addEventListener('click', (e) => manageTurma(e.target.dataset.id))
            );

        } catch (error) {
            console.error('Erro ao carregar turmas:', error);
            mainContentArea.innerHTML = `<h2>Gerenciar Turmas</h2>
                                <div class="alert alert-danger">Erro ao carregar turmas: ${error.message}</div>`;
        }
    }

    async function showTurmaForm(id = null) {
        let turma = {};
        let disciplinasDisponiveis = [];
        let professoresDisponiveis = [];
        let alunosDisponiveis = [];

        try {
            if (id) {
                mainContentArea.innerHTML = `<h2>Editando Turma...</h2><p>Carregando dados da turma...</p>`;
                turma = await fetchData(`${API_BASE_URL}/turmas/${id}`);
            }

            disciplinasDisponiveis = await fetchData(`${API_BASE_URL}/disciplinas`);
            professoresDisponiveis = await fetchData(`${API_BASE_URL}/professores`);
            alunosDisponiveis = await fetchData(`${API_BASE_URL}/alunos`);

            mainContentArea.innerHTML = `
        <h2>${id ? 'Editar Turma' : 'Nova Turma'}</h2>
        <form id="turmaForm">
            <input type="hidden" id="turmaId" value="${turma.id || ''}">
            
            <div class="mb-3">
                <label for="periodo" class="form-label">Período *</label>
                <input type="text" class="form-control" id="periodo" 
                       value="${turma.periodo || ''}" required 
                       placeholder="Ex: 2024.1, 2024.2, etc.">
            </div>
            
            <div class="mb-3">
                <label for="disciplinasIds" class="form-label">Disciplinas</label>
                <select multiple class="form-control" id="disciplinasIds" size="5">
                    ${disciplinasDisponiveis.map(disciplina => `
                        <option value="${disciplina.id}" 
                            ${turma.disciplinasIds && turma.disciplinasIds.includes(disciplina.id) ? 'selected' : ''}>
                            ${disciplina.nome} - ${disciplina.codigo || ''}
                        </option>
                    `).join('')}
                </select>
                <small class="form-text text-muted">Segure Ctrl para selecionar múltiplas disciplinas</small>
            </div>
            
            <div class="mb-3">
                <label for="professoresIds" class="form-label">Professores</label>
                <select multiple class="form-control" id="professoresIds" size="5">
                    ${professoresDisponiveis.map(professor => `
                        <option value="${professor.id}" 
                            ${turma.professoresIds && turma.professoresIds.includes(professor.id) ? 'selected' : ''}>
                            ${professor.nome} - ${professor.matricula || ''}
                        </option>
                    `).join('')}
                </select>
                <small class="form-text text-muted">Segure Ctrl para selecionar múltiplos professores</small>
            </div>
            
            <div class="mb-3">
                <label for="alunosIds" class="form-label">Alunos</label>
                <select multiple class="form-control" id="alunosIds" size="5">
                    ${alunosDisponiveis.map(aluno => `
                        <option value="${aluno.id}" 
                            ${turma.alunosIds && turma.alunosIds.includes(aluno.id) ? 'selected' : ''}>
                            ${aluno.nome} - ${aluno.matricula || ''}
                        </option>
                    `).join('')}
                </select>
                <small class="form-text text-muted">Segure Ctrl para selecionar múltiplos alunos</small>
            </div>
            
            <button type="submit" class="btn btn-success">${id ? 'Atualizar' : 'Salvar'}</button>
            <button type="button" class="btn btn-secondary" id="cancelTurmaBtn">Cancelar</button>
        </form>
        <div id="formMessages"></div>
    `;

            document.getElementById('turmaForm').addEventListener('submit', handleTurmaSubmit);
            document.getElementById('cancelTurmaBtn').addEventListener('click', fetchAndRenderTurmas);

        } catch (error) {
            console.error('Erro ao carregar formulário:', error);
            mainContentArea.innerHTML = `<div class="alert alert-danger">Erro ao carregar formulário: ${error.message}</div>`;
        }
    }

    async function handleTurmaSubmit(event) {
        event.preventDefault();

        const formMessages = document.getElementById('formMessages');
        formMessages.innerHTML = '<div class="alert alert-info">Processando...</div>';

        try {
            const id = document.getElementById('turmaId')?.value;
            const periodo = document.getElementById('periodo')?.value;

            const disciplinasSelect = document.getElementById('disciplinasIds');
            const professoresSelect = document.getElementById('professoresIds');
            const alunosSelect = document.getElementById('alunosIds');

            const disciplinasIds = Array.from(disciplinasSelect.selectedOptions).map(option => parseInt(option.value));
            const professoresIds = Array.from(professoresSelect.selectedOptions).map(option => parseInt(option.value));
            const alunosIds = Array.from(alunosSelect.selectedOptions).map(option => parseInt(option.value));

            if (!periodo) {
                throw new Error('Preencha o período');
            }

            const turmaData = {
                periodo: periodo.trim(),
                disciplinasIds: disciplinasIds,
                professoresIds: professoresIds,
                alunosIds: alunosIds
            };

            if (id) {
                turmaData.id = parseInt(id);
            }

            console.log('Enviando dados da turma:', turmaData);

            const result = await fetchWithCSRF(
                id ? `${API_BASE_URL}/turmas/${id}` : `${API_BASE_URL}/turmas`,
                {
                    method: id ? 'PUT' : 'POST',
                    body: JSON.stringify(turmaData)
                }
            );

            formMessages.innerHTML = `<div class="alert alert-success">
                Turma ${id ? 'atualizada' : 'cadastrada'} com sucesso!
            </div>`;

            setTimeout(() => {
                fetchAndRenderTurmas();
            }, 1500);

        } catch (error) {
            console.error('Erro no submit:', error);
            formMessages.innerHTML = `<div class="alert alert-danger">
                Erro ao ${document.getElementById('turmaId')?.value ? 'atualizar' : 'cadastrar'} turma: ${error.message}
            </div>`;
        }
    }

    async function manageTurma(id) {
        try {
            const turma = await fetchData(`${API_BASE_URL}/turmas/${id}`);
            const disciplinas = await fetchData(`${API_BASE_URL}/disciplinas`);
            const professores = await fetchData(`${API_BASE_URL}/professores`);
            const alunos = await fetchData(`${API_BASE_URL}/alunos`);

            mainContentArea.innerHTML = `
        <h2>Gerenciar Turma: ${turma.periodo}</h2>
        
        <div class="row">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h5>Disciplinas da Turma</h5>
                    </div>
                    <div class="card-body">
                        ${turma.disciplinasIds && turma.disciplinasIds.length > 0 ?
                `<ul class="list-group">
                                ${turma.disciplinasIds.map(disciplinaId => {
                    const disciplina = disciplinas.find(d => d.id === disciplinaId);
                    return disciplina ? `<li class="list-group-item">${disciplina.nome}</li>` : '';
                }).join('')}
                            </ul>` :
                '<p class="text-muted">Nenhuma disciplina associada</p>'
            }
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h5>Professores da Turma</h5>
                    </div>
                    <div class="card-body">
                        ${turma.professoresIds && turma.professoresIds.length > 0 ?
                `<ul class="list-group">
                                ${turma.professoresIds.map(professorId => {
                    const professor = professores.find(p => p.id === professorId);
                    return professor ? `<li class="list-group-item">${professor.nome}</li>` : '';
                }).join('')}
                            </ul>` :
                '<p class="text-muted">Nenhum professor associado</p>'
            }
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h5>Alunos da Turma</h5>
                    </div>
                    <div class="card-body">
                        ${turma.alunosIds && turma.alunosIds.length > 0 ?
                `<ul class="list-group">
                                ${turma.alunosIds.map(alunoId => {
                    const aluno = alunos.find(a => a.id === alunoId);
                    return aluno ? `<li class="list-group-item">${aluno.nome}</li>` : '';
                }).join('')}
                            </ul>` :
                '<p class="text-muted">Nenhum aluno matriculado</p>'
            }
                    </div>
                </div>
            </div>
        </div>
        
        <div class="mt-3">
            <button class="btn btn-warning" onclick="showTurmaForm(${id})">Editar Turma</button>
            <button class="btn btn-secondary" onclick="fetchAndRenderTurmas()">Voltar</button>
        </div>
    `;
        } catch (error) {
            console.error('Erro ao gerenciar turma:', error);
            mainContentArea.innerHTML = `<div class="alert alert-danger">Erro ao carregar dados da turma: ${error.message}</div>`;
        }
    }

    async function deleteTurma(id) {
        if (!confirm('Tem certeza que deseja deletar esta turma? Isso removerá todas as associações com disciplinas, professores e alunos!')) {
            return;
        }

        try {
            await fetchWithCSRF(`${API_BASE_URL}/turmas/${id}`, {
                method: 'DELETE'
            });

            alert('Turma deletada com sucesso!');
            fetchAndRenderTurmas();

        } catch (error) {
            console.error('Erro ao deletar:', error);
            alert(`Erro ao deletar turma: ${error.message}`);
        }
    }

    // ALUNOS
    async function fetchAndRenderAlunos() {
        mainContentArea.innerHTML = '<h2>Gerenciar Alunos</h2><p>Carregando alunos...</p>';

        try {
            const alunos = await fetchData(`${API_BASE_URL}/alunos`);

            if (!alunos || alunos.length === 0) {
                mainContentArea.innerHTML = `
                <h2>Gerenciar Alunos</h2>
                <p>Nenhum aluno encontrado.</p>
                <button id="addAlunoBtn" class="btn btn-primary mb-3">Adicionar Aluno</button>
            `;
                document.getElementById('addAlunoBtn')?.addEventListener('click', () => renderAlunoFormCadastro());
                return;
            }

            let htmlContent = `
            <h2>Gerenciar Alunos</h2>
            <button id="addAlunoBtn" class="btn btn-primary mb-3">Adicionar Aluno</button>
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Nome Completo</th>
                            <th>Matrícula</th>
                            <th>Usuário</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

            alunos.forEach(aluno => {
                htmlContent += `
                <tr>
                    <td>${aluno.nome}</td>
                    <td>${aluno.matricula}</td>
                    <td>${aluno.username}</td>
                    <td>
                        <button class="btn btn-info btn-sm view-btn" data-id="${aluno.id}">Ver Informações</button>
                        <button class="btn btn-warning btn-sm edit-btn" data-id="${aluno.id}">Editar</button>
                        <button class="btn btn-danger btn-sm delete-btn" data-id="${aluno.id}">Excluir</button>
                    </td>
                </tr>
            `;
            });

            htmlContent += `</tbody></table></div>`;
            mainContentArea.innerHTML = htmlContent;

            document.getElementById('addAlunoBtn')?.addEventListener('click', () => renderAlunoFormCadastro());
            document.querySelectorAll('.view-btn').forEach(btn =>
                btn.addEventListener('click', (e) => showAlunoDetalhes(e.target.dataset.id))
            );
            document.querySelectorAll('.edit-btn').forEach(btn =>
                btn.addEventListener('click', (e) => showAlunoFormEdicao(e.target.dataset.id))
            );
            document.querySelectorAll('.delete-btn').forEach(btn =>
                btn.addEventListener('click', (e) => deleteAluno(e.target.dataset.id))
            );

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Gerenciar Alunos</h2><p class="text-danger">Erro ao carregar alunos: ${error.message}</p>`;
        }
    }

    async function showAlunoDetalhes(id) {
        mainContentArea.innerHTML = '<h2>Carregando informações do aluno...</h2>';

        try {
            const aluno = await fetchData(`${API_BASE_URL}/alunos/${id}`);

            let htmlContent = `
            <h2>Informações do Aluno</h2>
            <div class="card">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h5>Dados Pessoais</h5>
                            <p><strong>Nome Completo:</strong> ${aluno.nome}</p>
                            <p><strong>Matrícula:</strong> ${aluno.matricula}</p>
                            <p><strong>CPF:</strong> ${aluno.cpf}</p>
                            <p><strong>Data de Nascimento:</strong> ${aluno.dataNascimento}</p>
                        </div>
                        <div class="col-md-6">
                            <h5>Informações de Acesso</h5>
                            <p><strong>Nome de Usuário:</strong> ${aluno.username}</p>
                            <p><strong>Turma ID:</strong> ${aluno.turmaId || 'Não atribuída'}</p>
                        </div>
                    </div>
                    <hr>
                    <h5>Histórico Acadêmico</h5>
                    <div id="historico-aluno">
                        <p>Carregando histórico...</p>
                    </div>
                </div>
            </div>
            <button class="btn btn-secondary mt-3" id="voltarListaBtn">Voltar para Lista</button>
        `;

            mainContentArea.innerHTML = htmlContent;

            await carregarHistoricoAluno(id);

            document.getElementById('voltarListaBtn')?.addEventListener('click', fetchAndRenderAlunos);

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Erro</h2><p class="text-danger">Erro ao carregar informações do aluno: ${error.message}</p>`;
        }
    }

    async function carregarHistoricoAluno(alunoId) {
        try {
            const historicoContainer = document.getElementById('historico-aluno');
            historicoContainer.innerHTML = `
            <p>Funcionalidade de histórico em desenvolvimento.</p>
            <p>Em breve será possível visualizar todas as disciplinas cursadas, notas e frequência.</p>
        `;

        } catch (error) {
            const historicoContainer = document.getElementById('historico-aluno');
            historicoContainer.innerHTML = `<p class="text-danger">Erro ao carregar histórico: ${error.message}</p>`;
        }
    }

    async function renderAlunoFormCadastro() {
        mainContentArea.innerHTML = `
        <h2>Cadastrar Novo Aluno</h2>
        <form id="alunoFormCadastro">
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <label for="nome" class="form-label">Nome Completo *</label>
                        <input type="text" class="form-control" id="nome" name="nome" required>
                    </div>
                    <div class="mb-3">
                        <label for="matricula" class="form-label">Matrícula *</label>
                        <input type="text" class="form-control" id="matricula" name="matricula" required>
                    </div>
                    <div class="mb-3">
                        <label for="cpf" class="form-label">CPF *</label>
                        <input type="text" class="form-control" id="cpf" name="cpf" maxlength="11" required>
                    </div>
                    <div class="mb-3">
                        <label for="dataNascimento" class="form-label">Data de Nascimento *</label>
                        <input type="date" class="form-control" id="dataNascimento" name="dataNascimento" required>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="mb-3">
                        <label for="turmaId" class="form-label">Turma *</label>
                        <select class="form-control" id="turmaId" name="turmaId" required>
                            <option value="">Carregando turmas...</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="username" class="form-label">Nome de Usuário *</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Senha *</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                </div>
            </div>
            <button type="submit" class="btn btn-success">Cadastrar Aluno</button>
            <button type="button" class="btn btn-secondary" id="cancelCadastroBtn">Cancelar</button>
        </form>
    `;

        await fetchAndPopulateSelect(`${API_BASE_URL}/turmas`, 'turmaId', 'periodo');

        const alunoForm = document.getElementById('alunoFormCadastro');
        if (alunoForm) {
            alunoForm.addEventListener('submit', handleAlunoCadastroSubmit);
        }

        document.getElementById('cancelCadastroBtn')?.addEventListener('click', fetchAndRenderAlunos);
    }

    async function handleAlunoCadastroSubmit(event) {
        event.preventDefault();

        const formData = new FormData(event.target);

        const alunoData = {
            nome: formData.get('nome'),
            matricula: formData.get('matricula'),
            cpf: formData.get('cpf'),
            dataNascimento: formData.get('dataNascimento'),
            turmaId: parseInt(formData.get('turmaId')),
            username: formData.get('username'),
            password: formData.get('password')
        };

        console.log('Dados do aluno a serem enviados:', alunoData);

        try {
            await fetchWithCSRF(`${API_BASE_URL}/alunos`, {
                method: 'POST',
                body: JSON.stringify(alunoData)
            });

            alert('Aluno cadastrado com sucesso!');
            fetchAndRenderAlunos();
        } catch (error) {
            console.error('Erro completo:', error);
            alert(`Erro ao cadastrar aluno: ${error.message}`);
        }
    }

    async function showAlunoFormEdicao(id) {
        mainContentArea.innerHTML = '<h2>Editando Aluno...</h2><p>Carregando dados do aluno...</p>';

        try {
            const aluno = await fetchData(`${API_BASE_URL}/alunos/${id}`);

            mainContentArea.innerHTML = `
            <h2>Editar Aluno</h2>
            <form id="alunoFormEdicao">
                <input type="hidden" id="alunoId" value="${id}">
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label for="nome" class="form-label">Nome Completo</label>
                            <input type="text" class="form-control" id="nome" value="${aluno.nome}" required>
                        </div>
                        <div class="mb-3">
                            <label for="matricula" class="form-label">Matrícula</label>
                            <input type="text" class="form-control" id="matricula" value="${aluno.matricula}" required>
                        </div>
                        <div class="mb-3">
                            <label for="cpf" class="form-label">CPF</label>
                            <input type="text" class="form-control" id="cpf" value="${aluno.cpf}" required>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label for="dataNascimento" class="form-label">Data de Nascimento</label>
                            <input type="date" class="form-control" id="dataNascimento" value="${aluno.dataNascimento}" required>
                        </div>
                        <div class="mb-3">
                            <label for="turmaIdEdicao" class="form-label">Turma</label>
                            <select class="form-control" id="turmaIdEdicao" required>
                                <option value="">Carregando turmas...</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="username" class="form-label">Nome de Usuário</label>
                            <input type="text" class="form-control" id="username" value="${aluno.username}" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Nova Senha (Opcional)</label>
                            <input type="password" class="form-control" id="password" placeholder="Deixe vazio para manter a senha atual">
                        </div>
                    </div>
                </div>
                <button type="submit" class="btn btn-success">Salvar Alterações</button>
                <button type="button" class="btn btn-secondary" id="cancelEdicaoBtn">Cancelar</button>
            </form>
        `;

            await fetchAndPopulateSelect(`${API_BASE_URL}/turmas`, 'turmaIdEdicao', 'periodo', aluno.turmaId);

            document.getElementById('alunoFormEdicao')?.addEventListener('submit', (e) => handleAlunoEdicaoSubmit(e, id));
            document.getElementById('cancelEdicaoBtn')?.addEventListener('click', fetchAndRenderAlunos);

        } catch (error) {
            mainContentArea.innerHTML = `<h2>Erro</h2><p class="text-danger">Erro ao carregar dados do aluno: ${error.message}</p>`;
        }
    }

    async function handleAlunoEdicaoSubmit(event, id) {
        event.preventDefault();

        const alunoData = {
            nome: document.getElementById('nome').value,
            matricula: document.getElementById('matricula').value,
            cpf: document.getElementById('cpf').value,
            dataNascimento: document.getElementById('dataNascimento').value,
            turmaId: parseInt(document.getElementById('turmaIdEdicao').value),
            username: document.getElementById('username').value,
            password: document.getElementById('password').value || null
        };

        console.log('Dados para edição:', alunoData);

        try {
            await fetchWithCSRF(`${API_BASE_URL}/alunos/${id}`, {
                method: 'PUT',
                body: JSON.stringify(alunoData)
            });

            alert('Aluno atualizado com sucesso!');
            fetchAndRenderAlunos();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao atualizar aluno: ${error.message}`);
        }
    }

    async function deleteAluno(id) {
        if (!confirm('Tem certeza que deseja excluir este aluno?')) return;

        try {
            await fetchWithCSRF(`${API_BASE_URL}/alunos/${id}`, {
                method: 'DELETE'
            });

            alert('Aluno excluído com sucesso!');
            fetchAndRenderAlunos();
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao excluir aluno: ${error.message}`);
        }
    }

    // Lidar com o botão de logout
    if (logoutButton) {
        logoutButton.addEventListener('click', function (e) {
            e.preventDefault();

            // Criar um formulário dinâmico para fazer logout via POST
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/logout';

            // Adicionar CSRF token se estiver usando
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            if (csrfToken) {
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = csrfToken;
                form.appendChild(csrfInput);
            }

            document.body.appendChild(form);
            form.submit();
        });
    }

    // Carrega o conteúdo inicial ao carregar a página
    document.getElementById('home-link').click();
});