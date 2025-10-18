document.addEventListener('DOMContentLoaded', () => {
    // Seleciona todos os links da barra lateral
    const navLinks = document.querySelectorAll('.nav-link');
    const mainContentArea = document.getElementById('main-content-area');

    // Mapeamento dos links para as funções de busca
    const endpoints = {
        '/meu-historico': fetchAndRenderMeuHistorico,
        '/minhas-disciplinas': fetchAndRenderMinhasDisciplinas,
        '/meus-professores': fetchAndRenderMeusProfessores
    };

    navLinks.forEach(link => {
        link.addEventListener('click', async (event) => {
            event.preventDefault(); // Evita que o link recarregue a página

            // Remove a classe 'active' de todos os links e a adiciona ao link clicado
            navLinks.forEach(item => item.classList.remove('active'));
            event.target.classList.add('active');

            const href = event.target.getAttribute('href');

            if (endpoints[href]) {
                await endpoints[href]();
            } else if (href === '#') {
                mainContentArea.innerHTML = '';
            }
        });
    });

    // --- FUNÇÕES GERAIS ---
    async function fetchData(url) {
        mainContentArea.innerHTML = '<h2>Carregando...</h2>';
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                    // TODO: Adicionar o token JWT do aluno logado
                    // 'Authorization': `Bearer SEU_TOKEN_JWT`
                }
            });
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

    // --- MEU HISTÓRICO ACADÊMICO ---
    async function fetchAndRenderMeuHistorico() {
        // TODO: Substituir '1' pelo ID do aluno logado, obtido após a autenticação.
        const alunoId = 1;
        const historicos = await fetchData(`http://localhost:8080/api/historicos?alunoId=${alunoId}`);

        if (!historicos) return;

        let htmlContent = '<h2>Meu Histórico Acadêmico</h2>';
        htmlContent += '<table class="table table-striped"><thead><tr><th>Disciplina</th><th>Nota</th><th>Frequência</th></tr></thead><tbody>';

        historicos.forEach(h => {
            htmlContent += `
                <tr>
                    <td>${h.disciplinaId}</td>
                    <td>${h.nota}</td>
                    <td>${h.frequencia}</td>
                </tr>
            `;
        });
        htmlContent += '</tbody></table>';
        mainContentArea.innerHTML = htmlContent;
    }

    // --- MINHAS DISCIPLINAS ---
    async function fetchAndRenderMinhasDisciplinas() {
        // TODO: Substituir '1' pelo ID do aluno logado
        const alunoId = 1;
        const turmas = await fetchData(`http://localhost:8080/api/turmas?alunoId=${alunoId}`);

        if (!turmas) return;

        let htmlContent = '<h2>Minhas Disciplinas</h2>';
        htmlContent += '<p>Lista de todas as disciplinas nas turmas em que você está matriculado.</p>';
        htmlContent += '<table class="table table-striped"><thead><tr><th>ID</th><th>Nome</th><th>Carga Horária</th><th>Pré-Requisitos</th></tr></thead><tbody>';

        turmas.forEach(turma => {
            turma.disciplinas.forEach(disciplina => {
                htmlContent += `
                    <tr>
                        <td>${disciplina.id}</td>
                        <td>${disciplina.nome}</td>
                        <td>${disciplina.cargaHoraria}</td>
                        <td>${disciplina.preRequisitos.join(', ')}</td>
                    </tr>
                 `;
            });
        });

        htmlContent += '</tbody></table>';
        mainContentArea.innerHTML = htmlContent;
    }

    // --- MEUS PROFESSORES ---
    async function fetchAndRenderMeusProfessores() {
        // TODO: Substituir '1' pelo ID do aluno logado
        const alunoId = 1;
        // Lógica de busca de professores associados ao aluno, via API
        const professores = await fetchData(`http://localhost:8080/api/professores?alunoId=${alunoId}`);

        if (!professores) return;

        let htmlContent = '<h2>Meus Professores</h2>';
        htmlContent += '<table class="table table-striped"><thead><tr><th>Nome</th><th>Especialização</th></tr></thead><tbody>';

        professores.forEach(prof => {
            htmlContent += `
                <tr>
                    <td>${prof.nome}</td>
                    <td>${prof.especializacao}</td>
                </tr>
            `;
        });
        htmlContent += '</tbody></table>';
        mainContentArea.innerHTML = htmlContent;
    }
});