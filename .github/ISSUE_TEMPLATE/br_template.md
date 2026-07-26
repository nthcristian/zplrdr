---
name: "Business Rule"
about: "Definição de lógica, validação ou restrição de negócio"
title: "[BR-XXX] Nome da Regra de Negócio"
labels: ["BR", "business-rule"]
assignees: ""
---

## 📌 Identificação

| Campo | Valor |
| :--- | :--- |
| **Código** | `BR-XXX` |
| **Módulo / Domínio** | Ex: *Vendas / Estoque / Impressão* |
| **Classificação** | `Restrição` / `Validação` / `Cálculo` / `Autorização` |
| **Prioridade** | `Alta` / `Média` / `Baixa` |

---

## 🎯 Descrição e Objetivo

> Breve resumo sobre o propósito desta regra no contexto do negócio e por que ela existe.

---

## 📐 Lógica de Execução

* **Contexto de Entrada:** O que aciona o disparo ou avaliação desta regra.
* **Critério de Validação:**
  * **Se** [Condição A] **e** [Condição B]
  * **Então** [Resultado permitido / Ação liberada]
  * **Senão** [Ação bloqueada / Execução de exceção]

---

## 🚨 Exceções e Mensagens de Erro

| Código do Erro | Condição de Disparo | Mensagem Exibida ao Usuário |
| :--- | :--- | :--- |
| `ERR-BRXXX-01` | [Condição que falhou] | *"Mensagem amigável para o usuário final."* |

---

## 🔗 Impactos e Dependências

* **Requisitos Funcionais Que Utilizam Esta Regra:** `FR-008`, `FR-012`
* **Regras de Negócio Relacionadas:** `BR-002`