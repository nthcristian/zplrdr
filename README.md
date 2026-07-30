# zplrdr

Converte etiquetas ZPL (Zebra Programming Language) para PDF e imprime em impressoras
térmicas de etiquetas.

## Funcionalidades

- **Conversão ZPL → PDF** via [API Labelary](http://api.labelary.com)
- **Predefinições** — configurações nomeadas de densidade e dimensões da etiqueta
- **Impressão TSPL nativa** — envia comandos TSPL diretamente para a impressora
  (TCP/IP ou dispositivo USB), sem usar drivers do sistema
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
| `print-pdf` | Imprime arquivos PDF existentes diretamente na impressora |
| `preset` | Gerencia predefinições: `list`, `create`, `show`, `set`, `delete` |
| `printers` | Lista dispositivos de impressora conectados localmente |

Exemplo de uso:

```bash
# Criar uma predefinição para etiquetas de 8dpmm, 4×6 polegadas
zplrdr preset create minha-etiqueta
zplrdr preset set minha-etiqueta --dpmm 8 --width 4 --height 6

# Converter um arquivo ZPL
zplrdr convert --preset minha-etiqueta -o saida.pdf etiqueta.zpl

# Converter e imprimir via TCP/IP
zplrdr print --preset minha-etiqueta --device tcp://192.168.1.100:9100 etiqueta.zpl

# Imprimir PDFs existentes
zplrdr print-pdf --device /dev/usb/lp0 --width 4 --height 6 --dpmm 8 etiqueta.pdf
```

## Estrutura do projeto

```
zplrdr/
├── rdr/          # Biblioteca principal — conversão ZPL, predefinições, API Labelary
├── cli/          # Aplicação CLI — interface de linha de comando (picocli)
├── prt/          # Serviço de impressão — comandos TSPL nativos (bitmap + dispositivo)
├── gui/          # Aplicação GUI — interface gráfica desktop (Swing)
└── gradle/       # Catálogo de versões e wrapper do Gradle
```

```
cli (application)  ──depende de──▶  :rdr, :prt
gui (application)  ──depende de──▶  :rdr, :prt
prt (java-library) ──depende de──▶  :rdr
rdr (java-library) ──independente
```

## Como funciona a impressão

O módulo `prt` converte cada página do PDF em um bitmap monocromático,
gera comandos TSPL (`SIZE`, `CLS`, `BITMAP`, `PRINT`) e envia os bytes
diretamente para a impressora:

- **TCP/IP**: `tcp://192.168.1.100:9100` (porta padrão JetDirect)
- **USB (Linux)**: `/dev/usb/lp0`
- **USB (macOS)**: `/dev/cu.usbmodem*`

Nenhum driver de impressão do sistema operacional é utilizado.

## Predefinições

As predefinições são armazenadas como arquivos JSON em `~/.local/share/zplrdr/`.
Cada arquivo contém um mapa chave-valor com os campos:

| Campo | Descrição | Exemplo |
|---|---|---|
| `dpmm` | Pontos por milímetro | `"8"` |
| `width` | Largura da etiqueta em polegadas | `"4"` |
| `height` | Altura da etiqueta em polegadas | `"6"` |
