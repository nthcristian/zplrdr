# zplrdr

Converte etiquetas ZPL (Zebra Programming Language) para PDF e imprime em impressoras
térmicas de etiquetas.

## Funcionalidades

- **Conversão ZPL → PDF** via [API Labelary](http://api.labelary.com)
- **Predefinições** — configurações nomeadas de densidade e dimensões da etiqueta
- **Impressão** — imprime diretamente em impressoras térmicas de etiquetas usando a API
  nativa de impressão do Java
- **Interface de linha de comando (CLI)** — adequada para scripts e automação
- **Interface gráfica (GUI)** — aplicativo desktop Swing com suporte completo a todas
  as operações

## Pré-requisitos

- JDK 25 (o Gradle baixa automaticamente via `foojay-resolver-convention`)

## Compilando e executando

```bash
# Compilar tudo
./gradlew build

# Executar todos os testes
./gradlew test

# Criar e executar a distribuição CLI
./gradlew :cli:installDist
./gradlew :cli:run --args="convert --preset minha-predefinicao etiqueta.zpl"

# Criar e executar a GUI
./gradlew :gui:installDist
./gradlew :gui:run
```

## Comandos da CLI

| Comando | Descrição |
|---|---|
| `convert` | Converte arquivos ZPL para PDF usando uma predefinição |
| `print` | Converte ZPL para PDF e imprime em uma única operação |
| `print-pdf` | Imprime arquivos PDF existentes em uma impressora de etiquetas |
| `preset` | Gerencia predefinições: `list`, `create`, `show`, `set`, `delete` |
| `printers` | Lista as impressoras disponíveis no sistema |

Exemplo de uso:

```bash
# Criar uma predefinição para etiquetas de 8dpmm, 4×6 polegadas
zplrdr preset create minha-etiqueta
zplrdr preset set minha-etiqueta --dpmm 8 --width 4 --height 6

# Converter um arquivo ZPL
zplrdr convert --preset minha-etiqueta -o saida.pdf etiqueta.zpl

# Converter e imprimir
zplrdr print --preset minha-etiqueta --printer "Tomate MDK-006" etiqueta.zpl
```

## Estrutura do projeto

```
zplrdr/
├── rdr/          # Biblioteca principal — conversão ZPL, predefinições, API Labelary
├── cli/          # Aplicação CLI — interface de linha de comando (picocli)
├── prt/          # Serviço de impressão — impressão de etiquetas via javax.print
├── gui/          # Aplicação GUI — interface gráfica desktop (Swing)
└── gradle/       # Catálogo de versões e wrapper do Gradle
```

```
cli (application)  ──depende de──▶  :rdr, :prt
gui (application)  ──depende de──▶  :rdr, :prt
prt (java-library) ──depende de──▶  :rdr
rdr (java-library) ──independente
```

## Predefinições

As predefinições são armazenadas como arquivos JSON em `~/.local/share/zplrdr/`.
Cada arquivo contém um mapa chave-valor com os campos:

| Campo | Descrição | Exemplo |
|---|---|---|
| `dpmm` | Pontos por milímetro | `"8"` |
| `width` | Largura da etiqueta em polegadas | `"1"` |
| `height` | Altura da etiqueta em polegadas | `"3"` |
