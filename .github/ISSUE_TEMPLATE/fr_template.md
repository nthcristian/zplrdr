---
name: "Functional Requirement"
about: "Especificação detalhada de uma funcionalidade do sistema"
title: "[FR-XXX] Nome do Requisito Funcional"
labels: ["FR", "functional-requirement"]
assignees: ""
---

## 📌 Identificação

| Campo | Valor |
| :--- | :--- |
| **Código** | `FR-XXX` |
| **Módulo** | Ex: *Impressão / Expedição / Autenticação* |
| **Ator Principal** | Ex: *Operador, Administrador, Sistema Cron* |
| **Prioridade** | `Must Have` / `Should Have` / `Could Have` |

---

## 👤 User Story

> **Como** [Ator / Perfil de Usuário],  
> **Eu quero** [Ação ou funcionalidade desejada],  
> **Para que** [Objetivo ou valor entregue ao negócio].

---

## ⚙️ Condições de Execução

* **Pré-condições:** O que deve estar satisfeito antes da execução (ex: *Usuário autenticado, Impressora conectada*).
* **Pós-condições:** O estado final do sistema após a execução (ex: *Status alterado para Impresso, Log gerado*).

---

## 🔄 Fluxo de Eventos

### Fluxo Principal (Caminho Feliz)
1. O ator solicita [Ação X].
2. O sistema valida [Condição Y].
3. O sistema executa [Processamento Z].
4. O sistema exibe a confirmação e finaliza a operação.

### Fluxo Alternativo (FA-01)
1. O ator escolhe a opção [Opção Secundária].
2. O sistema executa [Fluxo Alternativo].
3. O sistema retorna ao passo [N] do fluxo principal.

### Fluxo de Exceção (FE-01)
1. O sistema identifica [Falha de conexão / Dados inválidos].
2. O sistema aborta o processamento sem alterar dados.
3. O sistema exibe a mensagem de erro: `ERR-XXX-01`.

---

## 🔗 Vinculações

* **Regras de Negócio Relacionadas:** `BR-001`, `BR-014`
* **Requisitos Relacionados:** `FR-002`

---

## ✅ Critérios de Aceite (BDD / Gherkin)

```gherkin
Cenário 01: Execução com sucesso
Dado que [Pré-condição]
Quando [Ação do usuário]
Então [Resultado esperado no sistema]

Cenário 02: Falha na validação
Dado que [Pré-condição]
Quando [Ação do usuário com dados inválidos]
Então [Mensagem de erro exibida]