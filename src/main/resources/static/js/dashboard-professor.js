document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.querySelectorAll('.nav-link');
    const mainContentArea = document.getElementById('main-content-area');

    const endpoints = {
        '/minhas-disciplinas': fetchAndRenderMinhasDisciplinas,
        '/lancar-notas': fetchAndRenderFormularioNotas
    };

    navLinks.forEach(link => {
        link.addEventListener('click', async (event) => {
            event.preventDefault();

            navLinks.forEach(item => item.classList.remove('active'));
            event.target.classList.add('active');

            const href = event.target.getAttribute('href');

            if (endpoints[href]) {
                await endpoints[href]();
            } else if (href === '#') {
                mainContentArea.innerHTML = '<h2>Bem-vindo(a), Professor(a)!</h2><p>Selecione uma opção no menu lateral para visualizar seu conteúdo.</p>';
            }
        });
    });

    // --- FUNÇÕES GERAIS ---
    async function fetchData(url, options = {}) {
        mainContentArea.innerHTML = '<h2>Carregando...</h2>';
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error('Erro ao carregar os dados. Status: ' + response.status);
            }
            return await response.json();
        } catch (error) {
            mainContentArea.innerHTML = `<p class="text-danger">Erro ao carregar dados: ${error.message}</p>`;
            console.error('Erro:', error);
            throw error;
        }
    }

    // --- MINHAS DISCIPLINAS ---
    async function fetchAndRenderMinhasDisciplinas() {
        const professorId = 1; // TODO: Substituir pelo ID do professor logado, obtido via token
        const disciplinas = await fetchData(`http://localhost:8080/api/professores/${professorId}/disciplinas`);

        if (!disciplinas) return;

        let htmlContent = '<h2>Minhas Disciplinas</h2>';
        htmlContent += '<table class="table table-striped"><thead><tr><th>ID</th><th>Nome</th><th>Carga Horária</th></tr></thead><tbody>';

        disciplinas.forEach(d => {
            htmlContent += `
                <tr>
                    <td>${d.id}</td>
                    <td>${d.nome}</td>
                    <td>${d.cargaHoraria}</td>
                </tr>
            `;
        });
        htmlContent += '</tbody></table>';
        mainContentArea.innerHTML = htmlContent;
    }

    // --- LANÇAR NOTAS (Formulário) ---
    async function fetchAndRenderFormularioNotas() {
        const professorId = 1; // TODO: Substituir pelo ID do professor logado
        const turmas = await fetchData(`http://localhost:8080/api/professores/${professorId}/turmas`);

        let turmasOptions = turmas.map(t => `<option value="${t.id}">${t.periodo}</option>`).join('');

        mainContentArea.innerHTML = `
            <h2>Lançar Notas</h2>
            <form id="lancarNotasForm">
                <div class="mb-3">
                    <label for="turmaSelect" class="form-label">Selecione a Turma</label>
                    <select class="form-control" id="turmaSelect" required>
                        <option value="">-- Selecione uma turma --</option>
                        ${turmasOptions}
                    </select>
                </div>
                <div id="alunos-turma">
                    </div>
            </form>
        `;

        document.getElementById('turmaSelect').addEventListener('change', async (event) => {
            const turmaId = event.target.value;
            if (turmaId) {
                const turma = await fetchData(`http://localhost:8080/api/turmas/${turmaId}`);
                renderAlunosParaLancarNotas(turma.alunos);
            }
        });
    }

    function renderAlunosParaLancarNotas(alunos) {
        let alunosHtml = '<h3>Alunos da Turma</h3>';
        alunosHtml += '<table class="table table-bordered"><thead><tr><th>Aluno</th><th>Nota</th><th>Frequência</th></tr></thead><tbody>';

        alunos.forEach(aluno => {
            alunosHtml += `
                <tr>
                    <td>${aluno.nome}</td>
                    <td><input type="number" class="form-control" name="nota-${aluno.id}" min="0" max="10" step="0.1"></td>
                    <td><input type="number" class="form-control" name="frequencia-${aluno.id}" min="0" max="100"></td>
                </tr>
            `;
        });

        alunosHtml += '</tbody></table>';
        document.getElementById('alunos-turma').innerHTML = alunosHtml;
    }
});