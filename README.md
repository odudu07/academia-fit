# 🏋️ Academia Fit

![CI](https://github.com/odudu07/academia-fit/actions/workflows/ci.yml/badge.svg)

API REST para gerenciamento de academia, desenvolvida com **Java 21 + Spring Boot 3**.
Academias pequenas controlam matrículas e aulas coletivas em planilhas, o que gera alunos com plano vencido
frequentando, aulas superlotadas e reservas duplicadas. Esta API centraliza planos, alunos, matrículas, aulas e
reservas, aplicando as regras automaticamente.

## Tecnologias
Java 21 · Spring Boot 3.3 · Spring Web · Spring Data JPA · Bean Validation · H2 · JUnit 5 · Mockito · Maven · GitHub Actions

## Como executar
```bash
mvn spring-boot:run        # sobe em http://localhost:8080
mvn verify                 # compila e executa os testes
```
Console do banco (H2): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:academia`, usuário `sa`, senha vazia.
O arquivo [`requests.http`](requests.http) contém a sequência completa de requisições para a demonstração.

## Modelo de dados
```
Plano 1 ─── N Matricula N ─── 1 Aluno 1 ─── N Reserva N ─── 1 Aula
```
Aluno e Aula têm relacionamento muito-para-muitos, resolvido pela entidade `Reserva` (que guarda o status).

| Entidade | Campos |
|---|---|
| Plano | id, nome (único), valorMensal, duracaoMeses |
| Aluno | id, nome, email (único), cpf (único), dataNascimento |
| Matricula | id, aluno, plano, dataInicio, dataFim, status (ATIVA/CANCELADA) |
| Aula | id, nome, instrutor, dataHora, capacidade |
| Reserva | id, aluno, aula, status (CONFIRMADA/CANCELADA) |

## Endpoints
| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/planos` | Cria plano | 201 | 400, 409 |
| GET | `/planos`, `/planos/{id}` | Lista / busca plano | 200 | 404 |
| DELETE | `/planos/{id}` | Remove plano sem matrículas | 204 | 404, 409 |
| POST | `/alunos` | Cadastra aluno | 201 | 400, 409 |
| PUT | `/alunos/{id}` | Atualiza aluno | 200 | 400, 404, 409 |
| GET | `/alunos`, `/alunos/{id}` | Lista / busca aluno | 200 | 404 |
| POST | `/matriculas` | Matricula aluno em um plano | 201 | 400, 404, 409, 422 |
| GET | `/matriculas?alunoId=` | Lista matrículas | 200 | |
| GET | `/matriculas/{id}` | Busca matrícula | 200 | 404 |
| PATCH | `/matriculas/{id}/cancelar` | Cancela matrícula | 200 | 404, 422 |
| POST | `/aulas` | Cria aula | 201 | 400 |
| GET | `/aulas`, `/aulas/{id}` | Lista / busca aula (com vagas disponíveis) | 200 | 404 |
| POST | `/reservas` | Reserva vaga em aula | 201 | 400, 404, 409, 422 |
| GET | `/reservas?alunoId=` | Lista reservas | 200 | |
| GET | `/reservas/{id}` | Busca reserva | 200 | 404 |
| PATCH | `/reservas/{id}/cancelar` | Cancela reserva | 200 | 404, 422 |

### Códigos de erro
| Status | Quando |
|---|---|
| 400 | Validação falhou (campos obrigatórios, e-mail/CPF inválido, data inválida, JSON malformado) |
| 404 | Recurso não encontrado |
| 409 | Conflito: aula lotada, reserva duplicada, matrícula ativa existente, e-mail/CPF/nome duplicado |
| 422 | Regra de negócio violada |

Exemplo de resposta de erro:
```json
{
  "timestamp": "2030-01-10T09:30:00",
  "status": 409,
  "mensagem": "A aula está lotada",
  "detalhes": null
}
```

## Regras de negócio
**Matrícula**
1. O aluno deve ter no mínimo 16 anos.
2. O aluno só pode ter uma matrícula ativa por vez.
3. A data de término é calculada pela duração do plano.
4. Apenas matrículas ativas podem ser canceladas.

**Reserva de aula**
1. Não é possível reservar aula que já ocorreu.
2. O aluno precisa ter matrícula ativa e vigente na data da aula.
3. O aluno não pode reservar a mesma aula duas vezes.
4. A aula não pode exceder a capacidade.
5. O cancelamento exige no mínimo 2h de antecedência.

## Testes
- `ReservaServiceTest`, `MatriculaServiceTest`, `PlanoServiceTest`, `AlunoServiceTest`: regras de negócio com **JUnit 5 + Mockito** (repositórios mockados).
- `AlunoControllerTest`, `ReservaControllerTest`: camada web com `@WebMvcTest` (status HTTP e validação).
- `AcademiaFitApplicationTests`: verifica se o contexto Spring sobe.

## Integração contínua
O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) compila o projeto e executa os testes a cada push
ou pull request na branch `main`.

## Estrutura
```
src/main/java/com/exemplo/academia
├── controller   # endpoints REST
├── service      # regras de negócio
├── repository   # Spring Data JPA
├── model        # entidades JPA
├── dto          # records de entrada/saída (+ validações)
└── exception    # exceções e @RestControllerAdvice
```
